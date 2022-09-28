package com.example.demo.readreplica.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "articles")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "article_gen")
    @SequenceGenerator(name = "article_gen", sequenceName = "articles_seq")
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime authored;

    private LocalDateTime published;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "article", orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
}
