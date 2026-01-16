package com.example.highrps.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity(name = "PostTag")
@Table(name = "post_tag")
public class PostTagEntity implements Serializable {

    @EmbeddedId
    private PostTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private PostEntity postEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private TagEntity tagEntity;

    @Column(name = "created_on")
    private LocalDateTime createdOn = LocalDateTime.now();

    public PostTagEntity() {
        this.createdOn = LocalDateTime.now();
    }

    public PostTagId getId() {
        return id;
    }

    public PostTagEntity setId(PostTagId id) {
        this.id = id;
        return this;
    }

    public PostEntity getPostEntity() {
        return postEntity;
    }

    public PostTagEntity setPostEntity(PostEntity postEntity) {
        this.postEntity = postEntity;
        return this;
    }

    public TagEntity getTagEntity() {
        return tagEntity;
    }

    public PostTagEntity setTagEntity(TagEntity tagEntity) {
        this.tagEntity = tagEntity;
        return this;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public PostTagEntity setCreatedOn(LocalDateTime createdOn) {
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
        PostTagEntity that = (PostTagEntity) o;
        return Objects.equals(this.postEntity, that.postEntity) && Objects.equals(this.tagEntity, that.tagEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.postEntity, this.tagEntity);
    }
}
