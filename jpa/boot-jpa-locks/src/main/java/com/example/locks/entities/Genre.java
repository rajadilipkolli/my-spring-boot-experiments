package com.example.locks.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "genres")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long genreId;

    private String genreName;

    @ManyToMany(mappedBy = "genres")
    private List<Movie> movies;

    public Genre setGenreId(Long genreId) {
        this.genreId = genreId;
        return this;
    }

    public Genre setGenreName(String genreName) {
        this.genreName = genreName;
        return this;
    }

    public Genre setMovies(List<Movie> movies) {
        this.movies = movies;
        return this;
    }
}
