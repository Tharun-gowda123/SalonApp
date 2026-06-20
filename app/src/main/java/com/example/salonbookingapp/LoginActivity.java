package com.example.salonbookingapp;

import com.google.android.material.textfield.TextInputEditText;
import android.widget.Button;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Toast;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText phoneInput, passwordInput;
    Button loginBtn;
    TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneInput = findViewById(R.id.phoneNumber);
        passwordInput = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        forgotPassword = findViewById(R.id.forgotPassword);

        loginBtn.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString();
            String pass = passwordInput.getText().toString();

            if (phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("EliteSalonPrefs", MODE_PRIVATE);
            String registeredPass = prefs.getString("pass_" + phone, null);

            if (registeredPass == null) {
                // New user - Register
                showNameDialog(phone, pass);
            } else if (registeredPass.equals(pass)) {
                // Existing user - Login
                String name = prefs.getString("name_" + phone, "User");
                saveSession(phone, name);
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show();
            }
        });

        forgotPassword.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Enter phone number first", Toast.LENGTH_SHORT).show();
            } else {
                showOtpDialog(phone);
            }
        });
    }

    private void showNameDialog(String phone, String pass) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Complete Registration");
        builder.setMessage("Enter your name to finish signing up:");
        
        final TextInputEditText input = new TextInputEditText(this);
        input.setHint("Full Name");
        builder.setView(input);

        builder.setPositiveButton("Finish", (dialog, which) -> {
            String name = input.getText().toString();
            if (!name.isEmpty()) {
                SharedPreferences prefs = getSharedPreferences("EliteSalonPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("pass_" + phone, pass);
                editor.putString("name_" + phone, name);
                editor.apply();

                saveSession(phone, name);
                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
        builder.show();
    }

    private void showOtpDialog(String phone) {
        String mockOtp = String.valueOf(1000 + (int)(Math.random() * 9000));
        Toast.makeText(this, "OTP Sent to " + phone + ": " + mockOtp, Toast.LENGTH_LONG).show();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify OTP");
        final TextInputEditText input = new TextInputEditText(this);
        input.setHint("Enter " + mockOtp);
        builder.setView(input);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            if (input.getText().toString().equals(mockOtp)) {
                Toast.makeText(this, "OTP Verified. Login now.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void saveSession(String phone, String name) {
        SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = userSession.edit();
        editor.putString("phone", phone);
        editor.putString("username", name);
        editor.apply();
    }
}