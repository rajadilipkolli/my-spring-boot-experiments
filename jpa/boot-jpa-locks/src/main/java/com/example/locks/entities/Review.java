package com.example.locks.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    private Double rating;

    private String review;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

}
