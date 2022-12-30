package com.example.graphql.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

@Entity
@Table(name = "posts")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class PostEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String title;

    @Column(length = 4096)
    private String content;

    private boolean published;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private LocalDateTime publishedAt;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", orphanRemoval = true)
    @Builder.Default
    private List<PostCommentEntity> comments = new ArrayList<>();

    @OneToOne(
            mappedBy = "post",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            optional = false)
    private PostDetailsEntity details;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostTagEntity> tags = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "author_id")
    private AuthorEntity author;

    public PostEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public void addComment(PostCommentEntity comment) {
        this.comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(PostCommentEntity comment) {
        this.comments.remove(comment);
        comment.setPost(null);
    }

    public void setDetails(PostDetailsEntity details) {
        if (details == null) {
            if (this.details != null) {
                this.details.setPost(null);
            }
        } else {
            details.setPost(this);
        }
        this.details = details;
    }

    public void addTag(TagEntity tag) {
        PostTagEntity postTag = new PostTagEntity(this, tag);
        this.tags.add(postTag);
    }

    public void removeTag(TagEntity tag) {
        for (Iterator<PostTagEntity> iterator = this.tags.iterator(); iterator.hasNext(); ) {
            PostTagEntity postTag = iterator.next();

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
        PostEntity post = (PostEntity) o;
        return id != null
                && title != null
                && Objects.equals(id, post.id)
                && Objects.equals(this.title, post.title);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
