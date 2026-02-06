package com.example.highrps.entities;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "post_details")
public class PostDetailsEntity extends Auditable {

    @Id
    private Long id;

    private String detailsKey;

    @Column(name = "created_by")
    private String createdBy;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id", nullable = false)
    private PostEntity postEntity;

    public PostDetailsEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getId() {
        return id;
    }

    public PostDetailsEntity setDetailsKey(String detailsKey) {
        this.detailsKey = detailsKey;
        return this;
    }

    public String getDetailsKey() {
        return detailsKey;
    }

    public PostDetailsEntity setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public PostDetailsEntity setPostEntity(PostEntity postEntity) {
        this.postEntity = postEntity;
        return this;
    }

    public PostEntity getPostEntity() {
        return postEntity;
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
        PostDetailsEntity other = (PostDetailsEntity) obj;
        return Objects.equals(this.createdBy, other.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.createdBy);
    }
}
