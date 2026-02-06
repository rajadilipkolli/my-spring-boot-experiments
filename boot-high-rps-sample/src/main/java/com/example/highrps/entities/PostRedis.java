package com.example.highrps.entities;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("posts:entity")
public class PostRedis extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String title;

    private String content;
    private Boolean published;
    private LocalDateTime publishedAt;

    public PostRedis() {}

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

    public PostRedis setPublished(Boolean published) {
        this.published = published;
        return this;
    }

    public Boolean getPublished() {
        return published;
    }

    public PostRedis setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
        return this;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}
