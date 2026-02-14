package com.example.highrps.entities;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("post-comments:entity")
public class PostCommentRedis extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id; // composite id: postId:commentId

    @Indexed
    private Long postId;

    @Indexed
    private Long commentId;

    private String title;
    private String content;
    private boolean published;
    private OffsetDateTime publishedAt;

    public PostCommentRedis() {}

    public PostCommentRedis setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public PostCommentRedis setPostId(Long postId) {
        this.postId = postId;
        return this;
    }

    public Long getPostId() {
        return postId;
    }

    public PostCommentRedis setCommentId(Long commentId) {
        this.commentId = commentId;
        return this;
    }

    public Long getCommentId() {
        return commentId;
    }

    public PostCommentRedis setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PostCommentRedis setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContent() {
        return content;
    }

    public PostCommentRedis setPublished(boolean published) {
        this.published = published;
        return this;
    }

    public boolean isPublished() {
        return published;
    }

    public PostCommentRedis setPublishedAt(OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
        return this;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }
}
