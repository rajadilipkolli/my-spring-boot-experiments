package com.example.graphql.querydsl.entities;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "PostComment")
@Table(name = "post_comment")
@Getter
@Setter
public class PostComment {

  @Id
  @GenericGenerator(
      name = "sequenceGenerator",
      strategy = "enhanced-sequence",
      parameters = {
        @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo"),
        @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
        @org.hibernate.annotations.Parameter(name = "increment_size", value = "5")
      })
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
  private Long id;

  private String review;

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
