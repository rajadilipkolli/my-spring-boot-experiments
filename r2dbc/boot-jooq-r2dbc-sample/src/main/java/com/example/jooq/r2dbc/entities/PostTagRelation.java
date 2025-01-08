package com.example.jooq.r2dbc.entities;

import java.util.UUID;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "posts_tags")
public class PostTagRelation {

    @Column("post_id")
    private UUID postId;

    @Column("tag_id")
    private UUID tagId;

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public UUID getTagId() {
        return tagId;
    }

    public void setTagId(UUID tagId) {
        this.tagId = tagId;
    }
}
