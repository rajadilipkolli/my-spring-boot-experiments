package com.example.locks.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "director")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
