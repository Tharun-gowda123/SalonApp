package com.example.salonbookingapp;

public class Booking {
    private String bookingId;
    private String userName;
    private String date;
    private String time;
    private int totalAmount;
    private String paymentId;

    public Booking() {
    }

    public Booking(String bookingId, String userName, String date, String time, int totalAmount, String paymentId) {
        this.bookingId = bookingId;
        this.userName = userName;
        this.date = date;
        this.time = time;
        this.totalAmount = totalAmount;
        this.paymentId = paymentId;
    }

    public String getBookingId() { return bookingId; }
    public String getUserName() { return userName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public int getTotalAmount() { return totalAmount; }
    public String getPaymentId() { return paymentId; }
}