package com.example.demo.readreplica.entities;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_gen")
    @SequenceGenerator(name = "comment_gen", sequenceName = "comment_seq")
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String comment;

    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;
}
