package com.example.learning.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "comments")
public class Comment {

    @Id
    @Column("id")
    private UUID id;

    @Column("content")
    private String content;

    @Column("post_id")
    private UUID postId;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column("version")
    @Version
    private Short version;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Short getVersion() {
        return version;
    }

    public void setVersion(Short version) {
        this.version = version;
    }

    public String toString() {
        return "Comment{" + "id="
                + id + ", content='"
                + content + '\'' + ", postId="
                + postId + ", createdAt="
                + createdAt + ", updatedAt="
                + updatedAt + ", version="
                + version + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private java.util.UUID id;
        private String content;
        private java.util.UUID postId;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        private Short version;

        public Builder id(java.util.UUID id) {
            this.id = id;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder postId(java.util.UUID postId) {
            this.postId = postId;
            return this;
        }

        public Builder createdAt(java.time.LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(java.time.LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder version(Short version) {
            this.version = version;
            return this;
        }

        public Comment build() {
            Comment c = new Comment();
            c.id = this.id;
            c.content = this.content;
            c.postId = this.postId;
            c.createdAt = this.createdAt;
            c.updatedAt = this.updatedAt;
            c.version = this.version;
            return c;
        }
    }
}
