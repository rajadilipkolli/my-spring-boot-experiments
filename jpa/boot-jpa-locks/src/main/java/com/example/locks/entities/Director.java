package com.example.locks.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "directors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
