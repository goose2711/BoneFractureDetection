package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.DatabaseHelper;

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonSignUp;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHelper = new DatabaseHelper(this);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                } else {
                    long result = dbHelper.addUser(username, password);
                    if (result != -1) {
                        Toast.makeText(SignUpActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close sign-up activity
                    } else {
                        Toast.makeText(SignUpActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
