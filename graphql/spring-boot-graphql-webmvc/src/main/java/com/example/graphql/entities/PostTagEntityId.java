package com.example.graphql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostTagEntityId implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "tag_id")
    private Long tagId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostTagEntityId that = (PostTagEntityId) o;
        return Objects.equals(this.postId, that.postId) && Objects.equals(this.tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.postId, this.tagId);
    }
}
