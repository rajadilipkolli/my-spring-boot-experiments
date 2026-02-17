package com.example.graphql.querydsl.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", orphanRemoval = true)
    private List<PostComment> comments = new ArrayList<>();

    @JoinColumn(name = "details_Id", nullable = false)
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "post", orphanRemoval = true)
    private PostDetails details;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> tags = new ArrayList<>();

    public Post setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getId() {
        return id;
    }

    public Post setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Post setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Post setComments(List<PostComment> comments) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        this.comments = comments;
        return this;
    }

    public List<PostComment> getComments() {
        return comments;
    }

    public Post setDetails(PostDetails details) {
        this.details = details;
        return this;
    }

    public PostDetails getDetails() {
        return details;
    }

    public Post setTags(List<PostTag> postTags) {
        if (postTags == null) {
            postTags = new ArrayList<>();
        }
        this.tags = postTags;
        return this;
    }

    public List<PostTag> getTags() {
        return tags;
    }

    public void addComment(PostComment comment) {
        this.comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(PostComment comment) {
        this.comments.remove(comment);
        comment.setPost(null);
    }

    public void addDetails(PostDetails details) {
        this.details = details;
        details.setPost(this);
    }

    public void removeDetails() {
        this.details.setPost(null);
        this.details = null;
    }

    public void addTag(Tag tag) {
        PostTag postTag = new PostTag().setPost(this).setTag(tag).setId(new PostTagId(this.getId(), tag.getId()));
        this.tags.add(postTag);
    }

    public void removeTag(Tag tag) {
        for (Iterator<PostTag> iterator = this.tags.iterator(); iterator.hasNext(); ) {
            PostTag postTag = iterator.next();

            if (postTag.getPost().equals(this) && postTag.getTag().equals(tag)) {
                iterator.remove();
                postTag.setPost(null);
                postTag.setTag(null);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Post post = (Post) o;
        return id != null && Objects.equals(id, post.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
