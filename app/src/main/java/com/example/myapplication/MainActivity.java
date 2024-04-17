package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is logged in
        if (isLoggedIn()) {
            // If logged in, navigate to another activity
//            startActivity(new Intent(MainActivity.this, AnotherActivity.class));
        } else {
            // If not logged in, navigate to the login activity
            startActivity(new Intent(MainActivity.this, LogInActivity.class));
        }

        // Finish the current activity to prevent the user from navigating back to it
        finish();
    }

    // Method to check if the user is logged in (you will implement this)
    private boolean isLoggedIn() {
        // Implement your logic to check if the user is logged in
        // For example, you could check if there's a stored session token or user credentials
        // If the user is logged in, return true; otherwise, return false
        return false; // For now, returning false assuming user is not logged in
    }
}
