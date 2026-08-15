package com.example.highrps.post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity(name = "PostTag")
@Table(name = "post_tag")
public class PostTagEntity implements Serializable {

    @EmbeddedId
    private PostTagId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity postEntity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private TagEntity tagEntity;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    public PostTagEntity() {
        this.createdOn = LocalDateTime.now();
    }

    public PostTagEntity(PostEntity postEntity, TagEntity tagEntity) {
        this.postEntity = postEntity;
        this.tagEntity = tagEntity;
        this.createdOn = LocalDateTime.now();
        this.id = new PostTagId(postEntity.getId(), tagEntity.getId());
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

        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        PostTagEntity that = (PostTagEntity) o;
        return id != null && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
