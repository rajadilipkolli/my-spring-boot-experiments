package com.example.graphql.model.response;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record PostCommentResponse(
        Long postId,
        Long commentId,
        String title,
        String content,
        boolean published,
        OffsetDateTime publishedAt,
        LocalDateTime createdAt) {
    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private Long postId;
        private Long commentId;
        private String title;
        private String content;
        private boolean published;
        private OffsetDateTime publishedAt;
        private LocalDateTime createdAt;
        public Builder postId(Long postId) {
            this.postId = postId;
            return this;
        }
        public Builder commentId(Long commentId) {
            this.commentId = commentId;
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
        public Builder published(boolean published) {
            this.published = published;
            return this;
        }
        public Builder publishedAt(OffsetDateTime publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        public PostCommentResponse build() {
            return new PostCommentResponse(postId, commentId, title, content, published, publishedAt, createdAt);
        }
    }
}
