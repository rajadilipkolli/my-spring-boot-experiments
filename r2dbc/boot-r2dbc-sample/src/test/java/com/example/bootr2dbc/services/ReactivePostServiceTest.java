package com.example.bootr2dbc.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.bootr2dbc.entities.ReactivePost;
import com.example.bootr2dbc.model.response.PagedResult;
import com.example.bootr2dbc.repositories.ReactivePostRepository;
import java.util.List;
import java.util.Optional;
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
class ReactivePostServiceTest {

    @Mock
    private ReactivePostRepository reactivePostRepository;

    @InjectMocks
    private ReactivePostService reactivePostService;

    @Test
    void findAllReactivePosts() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<ReactivePost> reactivePostPage = new PageImpl<>(List.of(getReactivePost()));
        given(reactivePostRepository.findAll(pageable)).willReturn(reactivePostPage);

        // when
        PagedResult<ReactivePost> pagedResult = reactivePostService.findAllReactivePosts(0, 10, "id", "asc");

        // then
        assertThat(pagedResult).isNotNull();
        assertThat(pagedResult.data()).isNotEmpty().hasSize(1);
        assertThat(pagedResult.hasNext()).isFalse();
        assertThat(pagedResult.pageNumber()).isEqualTo(1);
        assertThat(pagedResult.totalPages()).isEqualTo(1);
        assertThat(pagedResult.isFirst()).isTrue();
        assertThat(pagedResult.isLast()).isTrue();
        assertThat(pagedResult.hasPrevious()).isFalse();
        assertThat(pagedResult.totalElements()).isEqualTo(1);
    }

    @Test
    void findReactivePostById() {
        // given
        given(reactivePostRepository.findById(1L)).willReturn(Optional.of(getReactivePost()));
        // when
        Optional<ReactivePost> optionalReactivePost = reactivePostService.findReactivePostById(1L);
        // then
        assertThat(optionalReactivePost).isPresent();
        ReactivePost reactivePost = optionalReactivePost.get();
        assertThat(reactivePost.getId()).isEqualTo(1L);
        assertThat(reactivePost.getText()).isEqualTo("junitTest");
    }

    @Test
    void saveReactivePost() {
        // given
        given(reactivePostRepository.save(getReactivePost())).willReturn(getReactivePost());
        // when
        ReactivePost persistedReactivePost = reactivePostService.saveReactivePost(getReactivePost());
        // then
        assertThat(persistedReactivePost).isNotNull();
        assertThat(persistedReactivePost.getId()).isEqualTo(1L);
        assertThat(persistedReactivePost.getText()).isEqualTo("junitTest");
    }

    @Test
    void deleteReactivePostById() {
        // given
        willDoNothing().given(reactivePostRepository).deleteById(1L);
        // when
        reactivePostService.deleteReactivePostById(1L);
        // then
        verify(reactivePostRepository, times(1)).deleteById(1L);
    }

    private ReactivePost getReactivePost() {
        ReactivePost reactivePost = new ReactivePost();
        reactivePost.setId(1L);
        reactivePost.setText("junitTest");
        return reactivePost;
    }
}
