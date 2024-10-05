package com.example.Bookmyyshow.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Bookmyyshow.bean.BookingResponse;
import com.example.Bookmyyshow.bean.Bookingrequest;
import com.example.Bookmyyshow.bean.Movie;
import com.example.Bookmyyshow.bean.User;
import com.example.Bookmyyshow.service.Movieservice;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class ticketcontroller {

    @Autowired
    private Movieservice movieservice;
       //can see the movies
    @GetMapping("/movies")
    public ResponseEntity<List<Movie>> getMovies(@RequestParam String cinemaName) {
        List<Movie> movies = movieservice.getMovies(cinemaName);
        return new ResponseEntity<>(movies, HttpStatus.OK);
    }
     //retireve the movies by the id
    @GetMapping("/movies/{movieId}")
    public ResponseEntity<Movie> getMovieById(@RequestParam String cinemaName, @PathVariable String movieId) {
        Movie movie = movieservice.getMovieById(cinemaName, movieId);
        return new ResponseEntity<>(movie, HttpStatus.OK);
    }
    //get the users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getUserList() {
        List<User> users = movieservice.getUserList();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
    //get the users by the id
    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        User user = movieservice.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
    //checkseats 
    @GetMapping("/checkSeats")
    public ResponseEntity<Movie> checkSeats(@RequestParam String cinemaName, @RequestParam String movieId, @RequestParam String showTime) {
        Movie movie = movieservice.checkSeats(cinemaName, movieId, showTime);
        return new ResponseEntity<>(movie, HttpStatus.OK);
    }

    @PostMapping("/bookSeats/{cinemaName}")
	public BookingResponse bookSeats(@PathVariable String cinemaName, @RequestBody Bookingrequest bookingRequest,
			HttpServletResponse response) {
		BookingResponse bookingResponse = movieservice.bookSeats(cinemaName, bookingRequest);
		if (bookingResponse != null) {
			movieservice.generatePdf(response, bookingResponse, cinemaName);
		}
		return bookingResponse;
	}

    

	@GetMapping("/cancelBooking/{cinemaName}/{bookingId}")
	public String cancelMovie(@PathVariable String cinemaName, @PathVariable String bookingId) {
		return movieservice.cancelBooking(cinemaName, bookingId);
	}

	
  }
