package com.example.jooq.r2dbc.entities;

import com.example.jooq.r2dbc.model.Status;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
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

    @Column("status")
    private Status status = Status.DRAFT;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("created_by")
    @CreatedBy
    private String createdBy;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Short version;

    public Post() {}

    public UUID getId() {
        return id;
    }

    public Post setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Post setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Post setContent(String content) {
        this.content = content;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public Post setStatus(Status status) {
        this.status = status;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Post setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Post setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Post setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public Short getVersion() {
        return version;
    }

    public Post setVersion(Short version) {
        this.version = version;
        return this;
    }

    @Override
    public String toString() {
        return "Post{"
                + "id="
                + id
                + ", title='"
                + title
                + '\''
                + ", content='"
                + content
                + '\''
                + ", status="
                + status
                + ", createdAt="
                + createdAt
                + ", createdBy='"
                + createdBy
                + '\''
                + ", updatedAt="
                + updatedAt
                + ", version="
                + version
                + '}';
    }
}
