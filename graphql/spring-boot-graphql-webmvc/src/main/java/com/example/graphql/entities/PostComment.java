package com.example.graphql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

@Entity
@Table(name = "post_comments")
@Getter
@Setter
@Builder
@AllArgsConstructor
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String review;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    public PostComment() {
        this.createdOn = LocalDateTime.now();
    }

    public PostComment(String review) {
        this.review = review;
        this.createdOn = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PostComment postComment = (PostComment) o;
        return review != null && Objects.equals(review, postComment.review);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
