package com.example.locks.entities;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "genres")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long genreId;

    private String genreName;

    @ManyToMany(mappedBy = "genres")
    private List<Movie> movies;
}
