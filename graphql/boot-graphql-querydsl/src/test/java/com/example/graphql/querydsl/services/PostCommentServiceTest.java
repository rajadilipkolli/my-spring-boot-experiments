package com.example.graphql.querydsl.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.graphql.querydsl.entities.PostComment;
import com.example.graphql.querydsl.mapper.PostCommentMapper;
import com.example.graphql.querydsl.model.response.PostCommentResponse;
import com.example.graphql.querydsl.repositories.PostCommentRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceTest {

    @Mock
    private PostCommentRepository postCommentRepository;

    @Mock
    private PostCommentMapper postCommentMapper;

    @InjectMocks
    private PostCommentService postCommentService;

    @Test
    void findPostCommentById() {
        // given
        given(postCommentRepository.findById(1L)).willReturn(Optional.of(getPostComment()));
        given(postCommentMapper.toResponse(any(PostComment.class))).willReturn(getPostCommentResponse());
        // when
        Optional<PostCommentResponse> optionalPostComment = postCommentService.findPostCommentById(1L);
        // then
        assertThat(optionalPostComment).isPresent();
        PostCommentResponse postComment = optionalPostComment.get();
        assertThat(postComment.id()).isOne();
        assertThat(postComment.review()).isEqualTo("junitTest");
    }

    @Test
    void deletePostCommentById() {
        // given
        willDoNothing().given(postCommentRepository).deleteById(1L);
        // when
        postCommentService.deletePostCommentById(1L);
        // then
        verify(postCommentRepository, times(1)).deleteById(1L);
    }

    private PostComment getPostComment() {
        PostComment postComment = new PostComment();
        postComment.setId(1L);
        postComment.setReview("junitTest");
        return postComment;
    }

    private PostCommentResponse getPostCommentResponse() {
        return new PostCommentResponse(1L, "junitTest", LocalDateTime.now());
    }
}
