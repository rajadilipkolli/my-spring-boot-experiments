package com.example.highrps.entities;

import com.example.highrps.shared.AssertUtil;
import com.example.highrps.shared.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.hibernate.Hibernate;
import org.jspecify.annotations.Nullable;

@Entity
@Table(
        name = "posts",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uc_postentity_title_author_id",
                    columnNames = {"title", "author_id"})
        })
public class PostEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", length = 4096)
    private String content;

    @Column(name = "published", nullable = false)
    private boolean published;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Short version;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "postEntity", orphanRemoval = true)
    private List<PostCommentEntity> comments = new ArrayList<>();

    @OneToOne(mappedBy = "postEntity", cascade = CascadeType.ALL, optional = false)
    private PostDetailsEntity details;

    @OneToMany(mappedBy = "postEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTagEntity> tags = new ArrayList<>();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "author_id", nullable = false)
    private AuthorEntity authorEntity;

    // Protected no-arg constructor for JPA and tests
    protected PostEntity() {}

    // Public constructor with required fields and validation
    public PostEntity(String title, String content, AuthorEntity authorEntity) {
        this.title = AssertUtil.requireNotBlank(title, "Post title cannot be null or empty");
        this.content = AssertUtil.requireNotBlank(content, "Post content cannot be null or empty");
        this.authorEntity = authorEntity;
        this.published = false;
        this.comments = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    public PostEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getId() {
        return id;
    }

    public PostEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PostEntity setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContent() {
        return content;
    }

    public PostEntity setPublished(boolean published) {
        this.published = published;
        return this;
    }

    public boolean isPublished() {
        return published;
    }

    public PostEntity setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
        return this;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public PostEntity setComments(@Nullable List<PostCommentEntity> comments) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        this.comments = comments;
        return this;
    }

    public List<PostCommentEntity> getComments() {
        return comments;
    }

    public PostEntity setTags(@Nullable List<PostTagEntity> tags) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        this.tags = tags;
        return this;
    }

    public List<PostTagEntity> getTags() {
        return tags;
    }

    public PostEntity setAuthorEntity(AuthorEntity authorEntity) {
        this.authorEntity = authorEntity;
        return this;
    }

    public AuthorEntity getAuthorEntity() {
        return authorEntity;
    }

    public void addComment(PostCommentEntity comment) {
        this.comments.add(comment);
        comment.setPostEntity(this);
    }

    public void removeComment(PostCommentEntity comment) {
        this.comments.remove(comment);
        comment.setPostEntity(null);
    }

    public void setDetails(@Nullable PostDetailsEntity details) {
        if (details == null) {
            if (this.details != null) {
                this.details.setPostEntity(null);
            }
        } else {
            details.setPostEntity(this);
        }
        this.details = details;
    }

    public PostDetailsEntity getDetails() {
        return details;
    }

    public void addTag(TagEntity tagEntity) {
        PostTagEntity postTagEntity = new PostTagEntity(this, tagEntity);
        if (null == tags) {
            tags = new ArrayList<>();
        }
        this.tags.add(postTagEntity);
    }

    public void removeTag(TagEntity tagEntity) {
        for (Iterator<PostTagEntity> iterator = this.tags.iterator(); iterator.hasNext(); ) {
            PostTagEntity postTagEntity = iterator.next();

            if (postTagEntity.getPostEntity().equals(this)
                    && postTagEntity.getTagEntity().equals(tagEntity)) {
                iterator.remove();
                postTagEntity.setPostEntity(null);
                postTagEntity.setTagEntity(null);
            }
        }
    }

    // Domain methods
    public void publish() {
        if (!this.published) {
            this.published = true;
            this.publishedAt = LocalDateTime.now();
        }
    }

    public void unpublish() {
        this.published = false;
        this.publishedAt = null;
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
        PostEntity postEntity = (PostEntity) o;
        return id != null
                && title != null
                && Objects.equals(id, postEntity.id)
                && Objects.equals(this.title, postEntity.title);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
