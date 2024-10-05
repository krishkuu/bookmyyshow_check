package com.example.Bookmyyshow.repost;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Bookmyyshow.bean.BookingResponse;

@Repository
public interface bookingResponseRepository extends JpaRepository<BookingResponse, String> {
}
