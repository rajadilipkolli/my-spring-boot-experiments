package com.example.rest.template.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.rest.template.entities.Post;
import com.example.rest.template.model.response.PagedResult;
import com.example.rest.template.repositories.PostRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void findAllPosts() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Post> postPage = new PageImpl<>(List.of(getPost()));
        given(postRepository.findAll(pageable)).willReturn(postPage);

        // when
        PagedResult<Post> pagedResult = postService.findAllPosts(0, 10, "id", "asc");

        // then
        assertThat(pagedResult).isNotNull();
        assertThat(pagedResult.data()).isNotEmpty().hasSize(1);
        assertThat(pagedResult.hasNext()).isFalse();
        assertThat(pagedResult.pageNumber()).isOne();
        assertThat(pagedResult.totalPages()).isOne();
        assertThat(pagedResult.isFirst()).isTrue();
        assertThat(pagedResult.isLast()).isTrue();
        assertThat(pagedResult.hasPrevious()).isFalse();
        assertThat(pagedResult.totalElements()).isOne();
    }

    @Test
    void findPostById() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(getPost()));
        // when
        Optional<Post> optionalPost = postService.findPostById(1L);
        // then
        assertThat(optionalPost).isPresent();
        Post post = optionalPost.get();
        assertThat(post.getId()).isOne();
        assertThat(post.getTitle()).isEqualTo("junitTest");
    }

    @Test
    @Disabled
    void savePost() {
        // given
        given(postRepository.save(getPost())).willReturn(getPost());
        // when
        Post persistedPost = postService.savePost(getPost());
        // then
        assertThat(persistedPost).isNotNull();
        assertThat(persistedPost.getId()).isOne();
        assertThat(persistedPost.getTitle()).isEqualTo("junitTest");
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
}
