package com.example.highrps.entities;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("posts:entity")
public class PostRedis extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id; // composite id: title:authorEmail

    @Indexed
    private String title;

    private String content;
    private boolean published;
    private LocalDateTime publishedAt;

    @Indexed
    private String authorEmail;

    public PostRedis() {}

    public PostRedis setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
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
}
