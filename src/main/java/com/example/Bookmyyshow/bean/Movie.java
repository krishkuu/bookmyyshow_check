package com.example.Bookmyyshow.bean;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int movieId;

    private String movieName;

    private String cinema; // Add this field if you want to query by 'cinema'

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Showtime> showtimes;

    private Integer moviePrice;

    // Default constructor
    public Movie() {
    }

    // Parameterized constructor
    public Movie(int movieId, String movieName, String cinema, Integer moviePrice) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.cinema = cinema;
        this.moviePrice = moviePrice;
    }

    // Getters and Setters
    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getCinema() {
        return cinema; // Add this getter
    }

    public void setCinema(String cinema) {
        this.cinema = cinema; // Add this setter
    }

    public List<Showtime> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(List<Showtime> showtimes) {
        this.showtimes = showtimes;
    }

    public Integer getMoviePrice() {
        return moviePrice;
    }

    public void setMoviePrice(Integer moviePrice) {
        this.moviePrice = moviePrice;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "movieId=" + movieId +
                ", movieName='" + movieName + '\'' +
                ", cinema='" + cinema + '\'' +  // Include 'cinema' in toString()
                ", moviePrice=" + moviePrice +
                '}';
    }
}
