//All Body Parts Work in this version in this version
package com.example.myapplication;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.myapplication.ml.Resnet50Bodyparts;
import com.example.myapplication.ml.Resnet50ElbowFrac;
import com.example.myapplication.ml.Resnet50HandFrac;
import com.example.myapplication.ml.Resnet50ShoulderFrac;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TestMLActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView textViewResult;
    private Bitmap imgBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tf_lite);

        imageView = findViewById(R.id.imageView);
        textViewResult = findViewById(R.id.textViewResult);
        Button selectImage = findViewById(R.id.button_select_image);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            }
        });
    }

    ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                        imgBitmap = BitmapFactory.decodeStream(imageStream);
                        imageView.setImageBitmap(imgBitmap);
                        processImage(imgBitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });

    //The below methods are to save the instance state of the image that the user selects. These methods are called
    // so that when the orientation changes or the user changes the app, the image and predicted text are not lost.
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the bitmap of the image if it's not null
        if (imgBitmap != null) {
            String imgBitmapBase64 = bitmapToBase64(imgBitmap);
            outState.putString("image_bitmap", imgBitmapBase64);
        }
        // Save text view content
        outState.putString("result_text", textViewResult.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the bitmap if available
        if (savedInstanceState.containsKey("image_bitmap")) {
            imgBitmap = base64ToBitmap(savedInstanceState.getString("image_bitmap"));
            imageView.setImageBitmap(imgBitmap);
        }
        // Restore text view content
        textViewResult.setText(savedInstanceState.getString("result_text"));
    }

    // Functions for converting a Bitmap object to a Base64 encoded string and vice versa to store in Bundle
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String encodedImage) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }


    private void processImage(Bitmap bitmap) {
        try {
            // Models instances
            Resnet50Bodyparts modelParts = Resnet50Bodyparts.newInstance(getApplicationContext());
            Resnet50ElbowFrac modelElbow = Resnet50ElbowFrac.newInstance(getApplicationContext());
            Resnet50HandFrac modelHand = Resnet50HandFrac.newInstance(getApplicationContext());
            Resnet50ShoulderFrac modelShoulder = Resnet50ShoulderFrac.newInstance(getApplicationContext());

            // Preparing input for the Body Parts model
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
            ByteBuffer byteBuffer = preprocessImageForResNet50(resizedBitmap);
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);


            // Predicting body part
            Resnet50Bodyparts.Outputs outputsParts = modelParts.process(inputFeature0);
            TensorBuffer outputFeature0Parts = outputsParts.getOutputFeature0AsTensorBuffer();
            float[] confidencesParts = outputFeature0Parts.getFloatArray();
            int bodyPartIndex = getMaxIndex(confidencesParts);

            String detectedPart;
            String fractureType;

            // Selecting the right model for fracture detection based on detected body part
            if (bodyPartIndex == 0) {
                detectedPart = "Elbow";
                Resnet50ElbowFrac.Outputs outputsFracture = modelElbow.process(inputFeature0);
                TensorBuffer outputFeature0Fracture = outputsFracture.getOutputFeature0AsTensorBuffer();
                float[] confidencesFracture = outputFeature0Fracture.getFloatArray();
                fractureType = (getMaxIndex(confidencesFracture) == 0) ? "Fractured" : "Normal";
            } else if (bodyPartIndex == 1) {
                detectedPart = "Hand";
                Resnet50HandFrac.Outputs outputsFracture = modelHand.process(inputFeature0);
                TensorBuffer outputFeature0Fracture = outputsFracture.getOutputFeature0AsTensorBuffer();
                float[] confidencesFracture = outputFeature0Fracture.getFloatArray();
                fractureType = (getMaxIndex(confidencesFracture) == 0) ? "Fractured" : "Normal";
            } else {
                detectedPart = "Shoulder";
                Resnet50ShoulderFrac.Outputs outputsFracture = modelShoulder.process(inputFeature0);
                TensorBuffer outputFeature0Fracture = outputsFracture.getOutputFeature0AsTensorBuffer();
                float[] confidencesFracture = outputFeature0Fracture.getFloatArray();
                fractureType = (getMaxIndex(confidencesFracture) == 0) ? "Fractured" : "Normal";
            }

            // Displaying the result
            String resultText = "Detected Body Part: " + detectedPart + "\nCondition: " + fractureType;
            textViewResult.setText(resultText);
            // Clean up
            modelParts.close();
            modelElbow.close();
            modelHand.close();
            modelShoulder.close();
        } catch (Exception e) {
            textViewResult.setText("Failed to process image: " + e.getMessage());
            e.printStackTrace();
        }    }

    private int getMaxIndex(float[] confidences) {
        int maxIndex = 0;
        for (int i = 1; i < confidences.length; i++) {
            if (confidences[i] > confidences[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[224 * 224];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < 224; i++) {
            for (int j = 0; j < 224; j++) {
                int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
            }
        }
        return byteBuffer;
    }
    private ByteBuffer preprocessImageForResNet50(Bitmap bitmap) {
        final int BATCH_SIZE = 1;
        final int PIXEL_SIZE = 3;
        final int IMAGE_MEAN = 128;
        final float IMAGE_STD = 128.0f;

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * BATCH_SIZE * 224 * 224 * PIXEL_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        int[] intValues = new int[224 * 224];
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < 224; ++i) {
            for (int j = 0; j < 224; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) - IMAGE_MEAN);
                byteBuffer.putFloat(((val >> 8) & 0xFF) - IMAGE_MEAN);
                byteBuffer.putFloat((val & 0xFF) - IMAGE_MEAN);
            }
        }
        return byteBuffer;
    }

}
