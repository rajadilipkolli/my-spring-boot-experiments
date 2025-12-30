package com.example.graphql.querydsl.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.graphql.querydsl.entities.Post;
import com.example.graphql.querydsl.mapper.PostMapper;
import com.example.graphql.querydsl.model.response.PostResponse;
import com.example.graphql.querydsl.repositories.PostRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    @Test
    void findPostById() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(getPost()));
        given(postMapper.toResponse(any(Post.class))).willReturn(getPostResponse());
        // when
        Optional<PostResponse> optionalPost = postService.findPostById(1L);
        // then
        assertThat(optionalPost).isPresent();
        PostResponse post = optionalPost.get();
        assertThat(post.id()).isOne();
        assertThat(post.title()).isEqualTo("junitTest");
    }

    @Test
    void deletePostById() {
        // given
        willDoNothing().given(postRepository).deleteById(1L);
        // when
        postService.deletePostById(1L);
        // then
        verify(postRepository, times(1)).deleteById(1L);
    }

    private Post getPost() {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("junitTest");
        return post;
    }

    private PostResponse getPostResponse() {
        return new PostResponse(
                1L, "junitTest", "junitContent", LocalDateTime.now(), new ArrayList<>(), new ArrayList<>());
    }
}
