package com.example.graphql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "post_comments")
@Getter
@Setter
@Builder
@AllArgsConstructor
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "postComment_id_generator")
    @SequenceGenerator(
            name = "postComment_id_generator",
            sequenceName = "postComment_id_seq",
            allocationSize = 100)
    private Long id;

    @Column(nullable = false)
    private String review;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @ManyToOne(fetch = FetchType.LAZY)
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return this.review != null && this.review.equals(((PostComment) o).getReview());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
