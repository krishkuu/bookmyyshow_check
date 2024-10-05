package com.example.Bookmyyshow.repost;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Bookmyyshow.bean.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String> {
    List<Movie> findByCinema(String cinema); // Adjust the property name here
}
