package com.example.Bookmyyshow.bean;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;

@Entity
// Ensure this matches the table name in your database
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "showtime_seq")
    @SequenceGenerator(name = "showtime_seq", sequenceName = "showtime_seq", initialValue = 1, allocationSize = 1)
    private Integer id;


    private String showTime; // Changed from LocalDateTime to String

    private Integer seats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Movie movie; // Assuming you want to keep the Movie class in the same package

    // Default constructor
    public Showtime() {
    }

    // Parameterized constructor
    public Showtime(Integer id, String showTime, Integer seats, Movie movie) {
        this.id = id;
        this.showTime = showTime;
        this.seats = seats;
        this.movie = movie;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    @Override
    public String toString() {
        return "Showtime{" +
                "id=" + id +
                ", showTime='" + showTime + '\'' +
                ", seats=" + seats +
                ", movie=" + movie +
                '}';
    }
}
