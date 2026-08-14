package com.example.highrps.post.domain;

import com.example.highrps.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Objects;
import org.hibernate.Hibernate;

@Entity
@Table(name = "post_details")
public class PostDetailsEntity extends BaseEntity {

    @Id
    private Long id;

    @Column(length = 255)
    private String detailsKey;

    @Column(name = "created_by")
    private String createdBy;

    @Version
    private Short version;

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

    public Short getVersion() {
        return version;
    }

    public PostDetailsEntity setVersion(Short version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || Hibernate.getClass(this) != Hibernate.getClass(obj)) {
            return false;
        }
        PostDetailsEntity other = (PostDetailsEntity) obj;
        return id != null && Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
