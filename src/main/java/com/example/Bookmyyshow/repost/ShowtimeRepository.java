package com.example.Bookmyyshow.repost;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Bookmyyshow.bean.Showtime;


@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Integer> {
    // Custom query methods can be added here if needed
}

