package com.example.graphql.querydsl.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "PostTag")
@Table(name = "post_tag")
@Setter
@Getter
public class PostTag {

  @EmbeddedId private PostTagId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("postId")
  private Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("tagId")
  private Tag tag;

  @Column(name = "created_on")
  private LocalDateTime createdOn = LocalDateTime.now();

  public PostTag() {
    this.createdOn = LocalDateTime.now();
  }

  public PostTag(Post post, Tag tag) {
    this.post = post;
    this.tag = tag;
    this.id = new PostTagId(post.getId(), tag.getId());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PostTag that = (PostTag) o;
    return Objects.equals(this.post, that.post) && Objects.equals(this.tag, that.tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.post, this.tag);
  }
}
