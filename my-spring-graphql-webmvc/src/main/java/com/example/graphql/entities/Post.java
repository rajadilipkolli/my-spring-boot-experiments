package com.example.graphql.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "posts")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Post implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_id_generator")
    @SequenceGenerator(
            name = "post_id_generator",
            sequenceName = "post_id_seq",
            allocationSize = 100)
    private Long id;

    private String title;

    @Column(length = 4096)
    private String content;

    private LocalDateTime createdOn;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "post", orphanRemoval = true)
    @Builder.Default
    private List<PostComment> comments = new ArrayList<>();

    @JoinColumn(name = "details_ID")
    @OneToOne(
            cascade = CascadeType.ALL,
            mappedBy = "post",
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private PostDetails details;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostTag> tags = new ArrayList<>();

    public Post() {
        this.createdOn = LocalDateTime.now();
    }

    public Post(Long id) {
        this.id = id;
        this.createdOn = LocalDateTime.now();
    }

    public Post(String title) {
        this.title = title;
        this.createdOn = LocalDateTime.now();
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
        PostTag postTag = new PostTag(this, tag);
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Post other = (Post) obj;
        return Objects.equals(this.details, other.details)
                && Objects.equals(this.title, other.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.details, this.title);
    }
}
