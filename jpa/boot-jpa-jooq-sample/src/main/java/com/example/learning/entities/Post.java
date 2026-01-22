package com.example.learning.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;

/**
 * Represents a blog post entity with associated comments, tags, and details.
 * This entity uses bidirectional relationships and implements proper relationship management.
 */
@Entity
@Table(name = "posts")
public class Post extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    private boolean published;

    private LocalDateTime publishedAt;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", orphanRemoval = true)
    @BatchSize(size = 20)
    private List<PostComment> comments = new ArrayList<>();

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, optional = false)
    private PostDetails details;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<PostTag> tags = new ArrayList<>();

    public Post() {}

    public Long getId() {
        return id;
    }

    public Post setId(Long id) {
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

    public boolean isPublished() {
        return published;
    }

    public Post setPublished(boolean published) {
        this.published = published;
        return this;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public Post setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
        return this;
    }

    public List<PostComment> getComments() {
        return comments;
    }

    public Post setComments(List<PostComment> comments) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        this.comments = comments;
        return this;
    }

    public PostDetails getDetails() {
        return details;
    }

    public void setDetails(PostDetails details) {
        if (details == null) {
            if (this.details != null) {
                this.details.setPost(null);
            }
        } else {
            details.setPost(this);
        }
        this.details = details;
    }

    public List<PostTag> getTags() {
        return tags;
    }

    public Post setTags(List<PostTag> tags) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        this.tags = tags;
        return this;
    }

    public Post publish() {
        this.published = true;
        this.publishedAt = LocalDateTime.now();
        return this;
    }

    public Post unpublish() {
        this.published = false;
        this.publishedAt = null;
        return this;
    }

    public void addComment(PostComment comment) {
        this.comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(PostComment comment) {
        this.comments.remove(comment);
        comment.setPost(null);
    }

    public void addTag(Tag tag) {
        PostTag postTag = new PostTag().setPost(this).setTag(tag).setId(new PostTagId(this.getId(), tag.getId()));
        this.tags.add(postTag);
    }

    public void removeTag(Tag tag) {
        if (tag == null) {
            return;
        }
        // This prevents ConcurrentModificationException
        for (Iterator<PostTag> iterator = tags.iterator(); iterator.hasNext(); ) {
            PostTag postTag = iterator.next();

            if (postTag.getPost().equals(this) && postTag.getTag().equals(tag)) {
                iterator.remove();
                postTag.setPost(null);
                postTag.setTag(null);
            }
        }
    }

    @Override
    public String toString() {
        return "Post{" + "id="
                + id + ", title='"
                + title + '\'' + ", published="
                + published + ", publishedAt="
                + publishedAt + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Post post = (Post) o;
        return id != null && title != null && Objects.equals(id, post.id) && Objects.equals(this.title, post.title);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
