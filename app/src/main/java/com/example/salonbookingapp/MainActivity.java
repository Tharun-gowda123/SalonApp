package com.example.salonbookingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.TextView;
import android.app.DatePickerDialog;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    CheckBox haircut, beard, massage;
    TextView totalPrice;
    AutoCompleteTextView timeSlot;
    Button confirmBtn, dateBtn;
    String selectedDate = "";
    int selectedYear, selectedMonth, selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        haircut = findViewById(R.id.haircut);
        beard = findViewById(R.id.beard);
        massage = findViewById(R.id.massage);
        totalPrice = findViewById(R.id.totalPrice);
        timeSlot = findViewById(R.id.timeSlot);
        confirmBtn = findViewById(R.id.confirmBtn);
        dateBtn = findViewById(R.id.dateBtn);

        // Set default date to Today
        final Calendar c = Calendar.getInstance();
        selectedYear = c.get(Calendar.YEAR);
        selectedMonth = c.get(Calendar.MONTH);
        selectedDay = c.get(Calendar.DAY_OF_MONTH);
        selectedDate = selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear;
        dateBtn.setText("Today: " + selectedDate);

        // Date Picker logic
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        (view, year1, monthOfYear, dayOfMonth) -> {
                            selectedYear = year1;
                            selectedMonth = monthOfYear;
                            selectedDay = dayOfMonth;
                            selectedDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1;
                            dateBtn.setText(selectedDate);
                            updateTimeSlots();
                        }, selectedYear, selectedMonth, selectedDay);
                
                // Restrict to current and future dates only
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        // Initialize slots immediately based on current time
        updateTimeSlots();

        // Dynamic price update
        View.OnClickListener priceListener = v -> updateTotalPrice();
        haircut.setOnClickListener(priceListener);
        beard.setOnClickListener(priceListener);
        massage.setOnClickListener(priceListener);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int total = calculateTotal();
                if (total == 0) {
                    Toast.makeText(MainActivity.this, "Please select at least one service", Toast.LENGTH_SHORT).show();
                } else if (selectedDate.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please select a date", Toast.LENGTH_SHORT).show();
                } else if (timeSlot.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please select a time slot", Toast.LENGTH_SHORT).show();
                } else {
                    String selectedTime = timeSlot.getText().toString();
                    
                    Intent intent = new Intent(MainActivity.this, BookingSummaryActivity.class);
                    intent.putExtra("DATE", selectedDate);
                    intent.putExtra("TIME", selectedTime);
                    intent.putExtra("TOTAL", total);
                    startActivity(intent);
                }
            }
        });

        Button historyBtn = findViewById(R.id.historyBtn);
        historyBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    private int calculateTotal() {
        int total = 0;
        if (haircut.isChecked()) total += 200;
        if (beard.isChecked()) total += 100;
        if (massage.isChecked()) total += 150;
        return total;
    }

    private void updateTotalPrice() {
        totalPrice.setText("Total Amount: ₹" + calculateTotal());
    }

    private void updateTimeSlots() {
        String[] allSlots = {"10:00 AM", "11:00 AM", "12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM", "06:00 PM"};
        List<String> availableSlots = new ArrayList<>();

        // Get already booked slots for this date
        SharedPreferences sharedPreferences = getSharedPreferences("SalonBookings", MODE_PRIVATE);
        String json = sharedPreferences.getString("bookings", null);
        List<String> bookedTimesForSelectedDate = new ArrayList<>();
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Booking>>() {}.getType();
            List<Booking> bookingList = gson.fromJson(json, type);
            if (bookingList != null) {
                for (Booking b : bookingList) {
                    if (b.getDate().equals(selectedDate)) {
                        bookedTimesForSelectedDate.add(b.getTime());
                    }
                }
            }
        }

        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH);
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        int currentHour = now.get(Calendar.HOUR_OF_DAY);

        boolean isToday = (selectedYear == currentYear && selectedMonth == currentMonth && selectedDay == currentDay);

        for (String slot : allSlots) {
            // Check if slot is already booked
            if (bookedTimesForSelectedDate.contains(slot)) {
                continue;
            }

            if (isToday) {
                int slotHour = parseHour(slot);
                if (slotHour > currentHour) {
                    availableSlots.add(slot);
                }
            } else {
                availableSlots.add(slot);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, availableSlots);
        timeSlot.setAdapter(adapter);
        timeSlot.setText("");
    }

    private int parseHour(String slot) {
        // Simple parser for "10:00 AM", "01:00 PM" etc.
        int hour = Integer.parseInt(slot.split(":")[0]);
        if (slot.contains("PM") && hour != 12) hour += 12;
        if (slot.contains("AM") && hour == 12) hour = 0;
        return hour;
    }
}