package com.example.demo.readreplica.entities;

import com.example.demo.readreplica.domain.ArticleDTO;
import com.example.demo.readreplica.domain.CommentDTO;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime authored;

    private LocalDateTime published;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "article", orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public Article() {}

    public Long getId() {
        return id;
    }

    public Article setId(Long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Article setTitle(String title) {
        this.title = title;
        return this;
    }

    public LocalDateTime getAuthored() {
        return authored;
    }

    public Article setAuthored(LocalDateTime authored) {
        this.authored = authored;
        return this;
    }

    public LocalDateTime getPublished() {
        return published;
    }

    public Article setPublished(LocalDateTime published) {
        this.published = published;
        return this;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public Article setComments(List<Comment> comments) {
        this.comments = comments;
        return this;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
        comment.setArticle(this);
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        comment.setArticle(null);
    }

    @Transient
    public ArticleDTO convertToArticleDTO() {
        return new ArticleDTO(
                getTitle(),
                getAuthored(),
                getPublished(),
                getComments().stream()
                        .map(comment -> new CommentDTO(comment.getComment()))
                        .toList());
    }
}
