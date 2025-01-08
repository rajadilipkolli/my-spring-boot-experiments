package com.example.jooq.r2dbc.entities;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "post_comments")
public class Comment {

    @Id
    @Column("id")
    private UUID id;

    @Column("content")
    private String content;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("post_id")
    private UUID postId;

    public UUID getId() {
        return id;
    }

    public Comment setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Comment setContent(String content) {
        this.content = content;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Comment setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public UUID getPostId() {
        return postId;
    }

    public Comment setPostId(UUID postId) {
        this.postId = postId;
        return this;
    }
}
