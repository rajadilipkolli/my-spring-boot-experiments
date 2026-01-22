package com.example.learning.entity;

import io.r2dbc.postgresql.codec.Json;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "posts")
public class Post {

    @Id
    @Column("id")
    private UUID id;

    @Column("title")
    private String title;

    @Column("content")
    private String content;

    @Column("metadata")
    private Json metadata;

    @Column("status")
    private Post.Status status;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Short version;

    @Transient
    private List<Comment> comments = new ArrayList<>(); // This field is transient and will be populated manually

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Json getMetadata() {
        return metadata;
    }

    public void setMetadata(Json metadata) {
        this.metadata = metadata;
    }

    public Post.Status getStatus() {
        return status;
    }

    public void setStatus(Post.Status status) {
        this.status = status;
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

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String toString() {
        return "Post{" + "id="
                + id + ", title='"
                + title + '\'' + ", content='"
                + content + '\'' + ", metadata="
                + metadata + ", status="
                + status + ", createdAt="
                + createdAt + ", updatedAt="
                + updatedAt + ", version="
                + version + ", comments="
                + comments + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private java.util.UUID id;
        private String title;
        private String content;
        private io.r2dbc.postgresql.codec.Json metadata;
        private Post.Status status;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        private Short version;
        private java.util.List<Comment> comments = new java.util.ArrayList<>();

        public Builder id(java.util.UUID id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder metadata(io.r2dbc.postgresql.codec.Json metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder status(Post.Status status) {
            this.status = status;
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

        public Builder comments(java.util.List<Comment> comments) {
            this.comments = comments;
            return this;
        }

        public Post build() {
            Post p = new Post();
            p.id = this.id;
            p.title = this.title;
            p.content = this.content;
            p.metadata = this.metadata;
            p.status = this.status;
            p.createdAt = this.createdAt;
            p.updatedAt = this.updatedAt;
            p.version = this.version;
            p.comments = this.comments != null ? this.comments : new java.util.ArrayList<>();
            return p;
        }
    }

    public enum Status {
        DRAFT,
        PENDING_MODERATION,
        PUBLISHED;
    }
}
