package com.example.locks.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "reviews")
public class Reviews {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long reviewId;

    private Double rating;

    private String review;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "movie_id")
    private Movie movie;
}
