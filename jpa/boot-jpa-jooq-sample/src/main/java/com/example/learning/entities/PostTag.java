package com.example.learning.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "PostTag")
@Table(name = "post_tag")
public class PostTag implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private PostTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    private Tag tag;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    public PostTag() {

        // No-args constructor for JPA
    }

    public PostTagId getId() {
        return id;
    }

    public PostTag setId(PostTagId id) {
        this.id = id;
        return this;
    }

    public Post getPost() {
        return post;
    }

    public PostTag setPost(Post post) {
        this.post = post;
        return this;
    }

    public Tag getTag() {
        return tag;
    }

    public PostTag setTag(Tag tag) {
        this.tag = tag;
        return this;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public PostTag setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
        return this;
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
