package com.example.salonbookingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BookingSummaryActivity extends AppCompatActivity {

    TextView summaryDetails, totalAmountText;
    Button confirmBtn;
    String date, time, userName, uniqueBookingId;
    int totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_summary);

        summaryDetails = findViewById(R.id.summaryDetails);
        totalAmountText = findViewById(R.id.totalAmountText);
        confirmBtn = findViewById(R.id.payBtn);

        // Get the logged in user name
        SharedPreferences userPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userName = userPrefs.getString("username", "Guest User");

        date = getIntent().getStringExtra("DATE");
        time = getIntent().getStringExtra("TIME");
        totalAmount = getIntent().getIntExtra("TOTAL", 0);
        
        // Generate a Professional Unique ID (e.g., ELITE-8429)
        uniqueBookingId = "ELITE-" + (1000 + new Random().nextInt(9000));

        String details = "Booking Slip\n" +
                "ID: " + uniqueBookingId + "\n" +
                "Name: " + userName + "\n\n" +
                "Date: " + date + "\n" +
                "Time: " + time;
        
        summaryDetails.setText(details);
        totalAmountText.setText("₹" + totalAmount);
        confirmBtn.setText("Confirm Appointment");

        confirmBtn.setOnClickListener(v -> {
            saveBookingLocally();
            Toast.makeText(this, "Booking Confirmed! ID: " + uniqueBookingId, Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void saveBookingLocally() {
        SharedPreferences sharedPreferences = getSharedPreferences("SalonBookings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String json = sharedPreferences.getString("bookings", null);
        Type type = new TypeToken<ArrayList<Booking>>() {}.getType();
        List<Booking> bookingList = gson.fromJson(json, type);

        if (bookingList == null) {
            bookingList = new ArrayList<>();
        }

        bookingList.add(new Booking(uniqueBookingId, userName, date, time, totalAmount, "CASH_AT_SALON"));

        String newJson = gson.toJson(bookingList);
        editor.putString("bookings", newJson);
        editor.apply();
    }
}