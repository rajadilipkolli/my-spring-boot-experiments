package com.example.graphql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "post_details")
public class PostDetailsEntity extends Auditable implements Serializable {

    @Id
    private Long id;

    private String detailsKey;

    @Column(name = "created_by")
    private String createdBy;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private PostEntity postEntity;

    public PostDetailsEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public PostDetailsEntity setDetailsKey(String detailsKey) {
        this.detailsKey = detailsKey;
        return this;
    }

    public PostDetailsEntity setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public PostDetailsEntity setPostEntity(PostEntity postEntity) {
        this.postEntity = postEntity;
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
        PostDetailsEntity other = (PostDetailsEntity) obj;
        return Objects.equals(this.createdBy, other.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.createdBy);
    }
}
