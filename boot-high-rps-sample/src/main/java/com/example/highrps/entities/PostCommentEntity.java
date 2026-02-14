package com.example.highrps.entities;

import com.example.highrps.shared.BaseEntity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "post_comments")
public class PostCommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "published", nullable = false)
    private boolean published;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Short version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity postEntity;

    public PostCommentEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getId() {
        return id;
    }

    public PostCommentEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PostCommentEntity setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContent() {
        return content;
    }

    public PostCommentEntity setPublished(boolean published) {
        this.published = published;
        return this;
    }

    public boolean isPublished() {
        return published;
    }

    public PostCommentEntity setPublishedAt(OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
        return this;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public PostCommentEntity setPostEntity(PostEntity postEntity) {
        this.postEntity = postEntity;
        return this;
    }

    public PostEntity getPostEntity() {
        return postEntity;
    }

    public Short getVersion() {
        return version;
    }

    public void setVersion(Short version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PostCommentEntity postCommentEntity = (PostCommentEntity) o;
        return id != null && Objects.equals(id, postCommentEntity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
