package com.example.learning.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "post_details")
public class PostDetails extends Auditable implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    private String detailsKey;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id", nullable = false)
    private Post post;

    public PostDetails() {}

    public Long getId() {
        return id;
    }

    public PostDetails setId(Long id) {
        this.id = id;
        return this;
    }

    public String getDetailsKey() {
        return detailsKey;
    }

    public PostDetails setDetailsKey(String detailsKey) {
        this.detailsKey = detailsKey;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public PostDetails setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public Post getPost() {
        return post;
    }

    public PostDetails setPost(Post post) {
        this.post = post;
        return this;
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
        PostDetails other = (PostDetails) obj;
        return Objects.equals(this.createdBy, other.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.createdBy);
    }
}
