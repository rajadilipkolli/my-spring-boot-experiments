package com.example.locks.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long movieId;

    private String movieTitle;

    private LocalDate releaseDate;

    private BigDecimal budget;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Reviews> reviews;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "director_id")
    private Director director;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private List<Genre> genres;

    @ManyToMany(
            mappedBy = "movies",
            cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Actor> actors;

    public Movie setMovieId(Long movieId) {
        this.movieId = movieId;
        return this;
    }

    public Long getMovieId() {
        return movieId;
    }

    public Movie setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
        return this;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public Movie setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
        return this;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public Movie setBudget(BigDecimal budget) {
        this.budget = budget;
        return this;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public Movie setReviews(List<Reviews> reviews) {
        this.reviews = reviews;
        return this;
    }

    public List<Reviews> getReviews() {
        return reviews;
    }

    public Movie setDirector(Director director) {
        this.director = director;
        return this;
    }

    public Director getDirector() {
        return director;
    }

    public Movie setGenres(List<Genre> genres) {
        this.genres = genres;
        return this;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public Movie setActors(List<Actor> actors) {
        this.actors = actors;
        return this;
    }

    public List<Actor> getActors() {
        return actors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Movie movie = (Movie) o;
        return movieId != null && Objects.equals(movieId, movie.movieId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
