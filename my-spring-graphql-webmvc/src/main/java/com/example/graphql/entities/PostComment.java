package com.example.graphql.entities;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "post comments")
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
