package com.example.jooq.r2dbc.entities;

import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "posts_tags")
public class PostTagRelation {

    @Column("post_id")
    private UUID postId;

    @Column("tag_id")
    private UUID tagId;

    public PostTagRelation() {}

    public PostTagRelation(UUID postId, UUID tagId) {
        this.postId = postId;
        this.tagId = tagId;
    }

    public UUID getPostId() {
        return postId;
    }

    public PostTagRelation setPostId(UUID postId) {
        this.postId = postId;
        return this;
    }

    public UUID getTagId() {
        return tagId;
    }

    public PostTagRelation setTagId(UUID tagId) {
        this.tagId = tagId;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("postId", postId).append("tagId", tagId).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostTagRelation)) return false;
        PostTagRelation that = (PostTagRelation) o;
        return Objects.equals(postId, that.postId) && Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, tagId);
    }
}
