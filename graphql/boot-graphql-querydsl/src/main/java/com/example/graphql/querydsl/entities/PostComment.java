package com.example.graphql.querydsl.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "post_comments")
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String review;

    private LocalDateTime createdOn;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    public PostComment setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getId() {
        return id;
    }

    public PostComment setReview(String review) {
        this.review = review;
        return this;
    }

    public String getReview() {
        return review;
    }

    public PostComment setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public PostComment setPost(Post post) {
        this.post = post;
        return this;
    }

    public Post getPost() {
        return post;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PostComment postComment = (PostComment) o;
        return id != null && Objects.equals(id, postComment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
