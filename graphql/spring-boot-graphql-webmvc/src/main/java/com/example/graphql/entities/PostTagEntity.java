package com.example.graphql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "PostTag")
@Table(name = "post_tag")
@Setter
@Getter
public class PostTagEntity {

    @EmbeddedId private PostTagEntityId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    private TagEntity tag;

    @Column(name = "created_on")
    private LocalDateTime createdOn = LocalDateTime.now();

    public PostTagEntity() {
        this.createdOn = LocalDateTime.now();
    }

    public PostTagEntity(PostEntity post, TagEntity tag) {
        this.post = post;
        this.tag = tag;
        this.id = new PostTagEntityId(post.getId(), tag.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PostTagEntity that = (PostTagEntity) o;
        return Objects.equals(this.post, that.post) && Objects.equals(this.tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.post, this.tag);
    }
}
