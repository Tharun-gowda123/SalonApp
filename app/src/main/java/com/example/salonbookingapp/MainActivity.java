package com.example.salonbookingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.TextView;
import android.app.DatePickerDialog;
import android.content.Intent;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    CheckBox haircut, beard, massage;
    TextView totalPrice;
    Spinner timeSlot;
    Button confirmBtn, dateBtn;
    String selectedDate = "";

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

        // Date Picker logic
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        (view, year1, monthOfYear, dayOfMonth) -> {
                            selectedDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1;
                            dateBtn.setText(selectedDate);
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        // Define the time slots
        String[] slots = {"10:00 AM", "11:00 AM", "12:00 PM", "01:00 PM", "02:00 PM", "04:00 PM", "05:00 PM", "06:00 PM"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, slots);
        timeSlot.setAdapter(adapter);

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
                } else {
                    String selectedTime = timeSlot.getSelectedItem().toString();
                    
                    Intent intent = new Intent(MainActivity.this, BookingSummaryActivity.class);
                    intent.putExtra("DATE", selectedDate);
                    intent.putExtra("TIME", selectedTime);
                    intent.putExtra("TOTAL", total);
                    startActivity(intent);
                }
            }
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
}