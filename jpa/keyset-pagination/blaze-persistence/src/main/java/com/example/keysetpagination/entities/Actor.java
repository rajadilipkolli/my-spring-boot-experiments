package com.example.keysetpagination.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "actors")
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "created_on", nullable = false)
    private LocalDate createdOn;

    public Actor setId(Long id) {
        this.id = id;
        return this;
    }

    public Actor setName(String name) {
        this.name = name;
        return this;
    }

    public Actor setCreatedOn(LocalDate createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Actor actor = (Actor) o;
        return id != null && Objects.equals(id, actor.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
