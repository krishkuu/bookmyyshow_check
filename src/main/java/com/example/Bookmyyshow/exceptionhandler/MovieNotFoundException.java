package com.example.Bookmyyshow.exceptionhandler;

public class MovieNotFoundException extends RuntimeException {
    public MovieNotFoundException(String message) {
        super(message);
    }
}
