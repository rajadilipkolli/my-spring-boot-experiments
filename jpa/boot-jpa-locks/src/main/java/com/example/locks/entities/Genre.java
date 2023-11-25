package com.example.locks.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "genre")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long genreId;

    private String genreName;

    @ManyToMany(mappedBy = "genres")
    private List<Movie> movies;
}
