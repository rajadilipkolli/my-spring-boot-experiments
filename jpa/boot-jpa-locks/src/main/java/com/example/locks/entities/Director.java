package com.example.locks.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "directors")
public class Director {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long directorId;

    private String directorName;

    private LocalDate dob;

    private String nationality;

    @OneToMany(mappedBy = "director")
    private List<Movie> movies;
}
