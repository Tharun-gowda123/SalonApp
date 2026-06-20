package com.example.salonbookingapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    RecyclerView historyRecyclerView;
    List<Booking> bookingList;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadBookings();
    }

    private void loadBookings() {
        SharedPreferences sharedPreferences = getSharedPreferences("SalonBookings", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("bookings", null);
        Type type = new TypeToken<ArrayList<Booking>>() {}.getType();
        bookingList = gson.fromJson(json, type);

        if (bookingList == null) {
            bookingList = new ArrayList<>();
            Toast.makeText(this, "No bookings found!", Toast.LENGTH_SHORT).show();
        }

        BookingAdapter adapter = new BookingAdapter(bookingList);
        historyRecyclerView.setAdapter(adapter);
    }

    private void createPdf(Booking booking) {
        // For older Android versions, we need runtime permission
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 500, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Header
        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        paint.setColor(Color.BLACK);
        canvas.drawText("ELITE SALON", 80, 50, paint);
        
        paint.setTextSize(12f);
        canvas.drawText("BOOKING SLIP", 95, 75, paint);

        // Content
        paint.setTextSize(12f);
        paint.setFakeBoldText(false);
        canvas.drawText("--------------------------------------", 30, 100, paint);
        canvas.drawText("Booking ID: " + booking.getBookingId(), 30, 130, paint);
        canvas.drawText("Customer: " + booking.getUserName(), 30, 160, paint);
        canvas.drawText("Date: " + booking.getDate(), 30, 190, paint);
        canvas.drawText("Time: " + booking.getTime(), 30, 220, paint);
        canvas.drawText("Amount: Rs. " + booking.getTotalAmount(), 30, 250, paint);
        canvas.drawText("Status: Paid at Salon", 30, 280, paint);
        canvas.drawText("--------------------------------------", 30, 310, paint);
        
        paint.setTextSize(10f);
        canvas.drawText("Thank you for choosing Elite Salon!", 50, 360, paint);
        canvas.drawText("Please show this slip at the reception.", 45, 380, paint);

        document.finishPage(page);

        String fileName = "EliteSalon_" + booking.getBookingId() + ".pdf";
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);

                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                if (uri != null) {
                    OutputStream fos = resolver.openOutputStream(uri);
                    document.writeTo(fos);
                    if (fos != null) fos.close();

                    contentValues.clear();
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                    resolver.update(uri, contentValues, null, null);
                    
                    Toast.makeText(this, "Saved to Downloads folder!", Toast.LENGTH_LONG).show();
                }
            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadsDir, fileName);
                document.writeTo(new FileOutputStream(file));
                Toast.makeText(this, "Saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

    class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
        List<Booking> list;
        BookingAdapter(List<Booking> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Booking booking = list.get(position);
            holder.date.setText("Date: " + booking.getDate());
            holder.time.setText("Time: " + booking.getTime());
            holder.amount.setText("Total Amount: ₹" + booking.getTotalAmount());
            holder.downloadBtn.setOnClickListener(v -> createPdf(booking));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView date, time, amount;
            Button downloadBtn;
            ViewHolder(View itemView) {
                super(itemView);
                date = itemView.findViewById(R.id.itemDate);
                time = itemView.findViewById(R.id.itemTime);
                amount = itemView.findViewById(R.id.itemAmount);
                downloadBtn = itemView.findViewById(R.id.downloadSlipBtn);
            }
        }
    }
}