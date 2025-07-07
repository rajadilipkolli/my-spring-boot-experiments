package com.example.locks.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "actors")
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long actorId;

    @Column(nullable = false)
    private String actorName;

    private LocalDate dob;

    private String nationality;

    @Version
    Short version = 0;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Movie> movies;

    public Actor setActorId(Long actorId) {
        this.actorId = actorId;
        return this;
    }

    public Long getActorId() {
        return actorId;
    }

    public Actor setActorName(String actorName) {
        this.actorName = actorName;
        return this;
    }

    public String getActorName() {
        return actorName;
    }

    public Actor setDob(LocalDate dob) {
        this.dob = dob;
        return this;
    }

    public LocalDate getDob() {
        return dob;
    }

    public Actor setNationality(String nationality) {
        this.nationality = nationality;
        return this;
    }

    public String getNationality() {
        return nationality;
    }

    public Actor setMovies(List<Movie> movies) {
        this.movies = movies;
        return this;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public Actor setVersion(Short version) {
        this.version = version;
        return this;
    }

    public Short getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Actor actor = (Actor) o;
        return actorId != null && Objects.equals(actorId, actor.actorId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
