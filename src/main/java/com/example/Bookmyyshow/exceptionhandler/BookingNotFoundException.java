package com.example.Bookmyyshow.exceptionhandler;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}
