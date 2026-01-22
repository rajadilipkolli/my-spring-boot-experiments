package com.example.locks.entities;

import jakarta.persistence.Column;
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

    @Column(name = "director_name", nullable = false)
    private String directorName;

    private LocalDate dob;

    private String nationality;

    @OneToMany(mappedBy = "director")
    private List<Movie> movies;

    public Director() {}

    public Director(Long directorId, String directorName, LocalDate dob, String nationality, List<Movie> movies) {
        this.directorId = directorId;
        this.directorName = directorName;
        this.dob = dob;
        this.nationality = nationality;
        this.movies = movies;
    }

    public Long getDirectorId() {
        return directorId;
    }

    public void setDirectorId(Long directorId) {
        this.directorId = directorId;
    }

    public String getDirectorName() {
        return directorName;
    }

    public void setDirectorName(String directorName) {
        this.directorName = directorName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }
}
