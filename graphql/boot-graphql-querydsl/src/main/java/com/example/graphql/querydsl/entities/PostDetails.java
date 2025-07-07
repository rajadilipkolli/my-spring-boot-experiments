package com.example.graphql.querydsl.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "PostDetails")
@Table(name = "post_details")
public class PostDetails {

    @Id
    private Long id;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Post post;

    public PostDetails setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getId() {
        return id;
    }

    public PostDetails setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public PostDetails setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public PostDetails setPost(Post post) {
        this.post = post;
        return this;
    }

    public Post getPost() {
        return post;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PostDetails other = (PostDetails) obj;
        return Objects.equals(this.createdBy, other.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.createdBy);
    }
}
