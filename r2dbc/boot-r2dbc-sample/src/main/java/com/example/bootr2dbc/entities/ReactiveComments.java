package com.example.bootr2dbc.entities;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "reactive_post_comments")
public class ReactiveComments {

    @Id
    @Column("id")
    private UUID id;

    @Column("title")
    private String title;

    @Column("content")
    private String content;

    @Column("post_id")
    private Long postId;

    private boolean published;

    @Column("published_at")
    private LocalDateTime publishedAt;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("created_by")
    @CreatedBy
    private String createdBy;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column("updated_by")
    @LastModifiedBy
    private String updatedBy;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private java.util.UUID id;
        private String title;
        private String content;
        private Long postId;
        private boolean published;
        private java.time.LocalDateTime publishedAt;
        private java.time.LocalDateTime createdAt;
        private String createdBy;
        private java.time.LocalDateTime updatedAt;
        private String updatedBy;

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

        public Builder postId(Long postId) {
            this.postId = postId;
            return this;
        }

        public Builder published(boolean published) {
            this.published = published;
            return this;
        }

        public Builder publishedAt(java.time.LocalDateTime publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public Builder createdAt(java.time.LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder updatedAt(java.time.LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public ReactiveComments build() {
            ReactiveComments rc = new ReactiveComments();
            rc.id = this.id;
            rc.title = this.title;
            rc.content = this.content;
            rc.postId = this.postId;
            rc.published = this.published;
            rc.publishedAt = this.publishedAt;
            rc.createdAt = this.createdAt;
            rc.createdBy = this.createdBy;
            rc.updatedAt = this.updatedAt;
            rc.updatedBy = this.updatedBy;
            return rc;
        }
    }
}
