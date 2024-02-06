package com.example.locks.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

@Entity
@Table(name = "actors")
@Getter
@NoArgsConstructor
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long actorId;

    private String actorName;

    private LocalDate dob;

    private String nationality;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Movie> movies;

    public Actor setActorId(Long actorId) {
        this.actorId = actorId;
        return this;
    }

    public Actor setActorName(String actorName) {
        this.actorName = actorName;
        return this;
    }

    public Actor setDob(LocalDate dob) {
        this.dob = dob;
        return this;
    }

    public Actor setNationality(String nationality) {
        this.nationality = nationality;
        return this;
    }

    public Actor setMovies(List<Movie> movies) {
        this.movies = movies;
        return this;
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
