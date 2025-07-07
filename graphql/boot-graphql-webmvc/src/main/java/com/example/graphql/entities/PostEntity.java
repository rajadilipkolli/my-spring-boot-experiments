package com.example.graphql.entities;

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
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "posts")
public class PostEntity extends Auditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String title;

    @Column(length = 4096)
    private String content;

    private boolean published;

    private LocalDateTime publishedAt;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "postEntity", orphanRemoval = true)
    private List<PostCommentEntity> comments = new ArrayList<>();

    @OneToOne(mappedBy = "postEntity", cascade = CascadeType.ALL, optional = false)
    private PostDetailsEntity details;

    @OneToMany(mappedBy = "postEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTagEntity> tags = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "author_id")
    private AuthorEntity authorEntity;

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

    public PostEntity setComments(List<PostCommentEntity> comments) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        this.comments = comments;
        return this;
    }

    public List<PostCommentEntity> getComments() {
        return comments;
    }

    public PostEntity setTags(List<PostTagEntity> tags) {
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

    public void setDetails(PostDetailsEntity details) {
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
