package com.example.highrps.post;

import com.example.highrps.post.domain.requests.PostDetailsRequest;
import com.example.highrps.post.domain.requests.TagRequest;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("posts:entity")
public class PostRedis implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Indexed
    private String title;

    private String content;
    private boolean published;
    private LocalDateTime publishedAt;

    @Indexed
    private String authorEmail;

    protected LocalDateTime createdAt;

    protected LocalDateTime modifiedAt;

    private PostDetailsRequest details;
    private List<TagRequest> tags;

    public PostRedis() {}

    public PostRedis setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getId() {
        return id;
    }

    public PostRedis setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PostRedis setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContent() {
        return content;
    }

    public PostRedis setPublished(boolean published) {
        this.published = published;
        return this;
    }

    public boolean isPublished() {
        return published;
    }

    public PostRedis setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
        return this;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public PostRedis setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
        return this;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public PostRedis setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public PostRedis setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
        return this;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    public PostRedis setDetails(PostDetailsRequest details) {
        this.details = details;
        return this;
    }

    public PostDetailsRequest getDetails() {
        return details;
    }

    public PostRedis setTags(List<TagRequest> tags) {
        this.tags = tags;
        return this;
    }

    public List<TagRequest> getTags() {
        return tags;
    }
}
