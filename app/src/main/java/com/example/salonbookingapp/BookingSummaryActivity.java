package com.example.salonbookingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BookingSummaryActivity extends AppCompatActivity {

    TextView summaryDetails;
    Button payBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_summary);

        summaryDetails = findViewById(R.id.summaryDetails);
        payBtn = findViewById(R.id.payBtn);

        String date = getIntent().getStringExtra("DATE");
        String time = getIntent().getStringExtra("TIME");
        int total = getIntent().getIntExtra("TOTAL", 0);

        String details = "Date: " + date + "\nTime: " + time + "\nTotal Amount: ₹" + total;
        summaryDetails.setText(details);

        payBtn.setOnClickListener(v -> {
            // Simulated payment
            Toast.makeText(this, "Payment of ₹50 Successful!", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Booking Confirmed!", Toast.LENGTH_LONG).show();
            finish();
        });
    }
}