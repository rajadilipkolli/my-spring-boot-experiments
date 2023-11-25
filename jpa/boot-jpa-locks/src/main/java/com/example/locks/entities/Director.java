package com.example.locks.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "director")
public class Director {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long directorId;

    private String directorName;

    private LocalDate dob;

    private String nationality;

    @OneToMany(mappedBy = "director")
    private List<Movie> movies;
}
