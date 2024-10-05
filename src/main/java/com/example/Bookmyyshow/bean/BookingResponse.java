package com.example.Bookmyyshow.bean;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;

@Entity
public class BookingResponse {

    @Id
    private String bookingId;
    private String movieId;
    private String showTime;
    private int numberOfTickets;
    private String status;
    private double totalAmount;
    private String currency;
    private String movieName; // Added this field
    @ManyToOne
    @JoinColumn(name = "user_id") // This should match the column in your database
    private User user;

    public BookingResponse() {
        // Default constructor needed by JPA
    }

    // Generate a unique booking ID automatically before persisting the entity
    @PrePersist
    public void generateBookingId() {
        if (this.bookingId == null || this.bookingId.isEmpty()) {
            this.bookingId = "BK" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public int getNumberOfTickets() {
        return numberOfTickets;
    }

    public void setNumberOfTickets(int numberOfTickets) {
        this.numberOfTickets = numberOfTickets;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMovieName() {
        return movieName; // Getter for movieName
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName; // Setter for movieName
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
