package com.example.highrps.postcomment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.highrps.author.domain.AuthorEntity;
import com.example.highrps.post.domain.PostEntity;
import org.junit.jupiter.api.Test;

class PostCommentEntityTest {

    @Test
    void shouldCreateCommentWithRequiredNaturalId() {
        PostEntity post = new PostEntity(
                "Post title", "Post content", new AuthorEntity("Jane", "Doe", "jane@test.com", 1234567890L));

        PostCommentEntity comment = new PostCommentEntity("Comment title", "Comment content", post, 42L);

        assertThat(comment.getCommentRefId()).isEqualTo(42L);
    }

    @Test
    void shouldRejectMissingCommentRefId() {
        PostEntity post = new PostEntity(
                "Post title", "Post content", new AuthorEntity("Jane", "Doe", "jane@test.com", 1234567890L));

        assertThatThrownBy(() -> new PostCommentEntity("Comment title", "Comment content", post, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("commentRefId");
    }
}
