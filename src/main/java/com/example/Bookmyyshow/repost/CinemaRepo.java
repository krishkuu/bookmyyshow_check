package com.example.Bookmyyshow.repost;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Bookmyyshow.bean.Cinema;





@Repository
public interface CinemaRepo extends JpaRepository<Cinema, String> {

}
