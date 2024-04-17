package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ml.Resnet50ElbowFrac;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class TestMLActivity extends AppCompatActivity {
    private Interpreter tflite;

    //@Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tf_lite);
//        try {
//            tflite = new Interpreter(loadModelFile());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
         try {
            Resnet50ElbowFrac model = Resnet50ElbowFrac.newInstance(context);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Resnet50ElbowFrac.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }

    }

    @Override
    protected void onDestroy() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
        super.onDestroy();
    }
}

