package com.example.bootr2dbc.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.bootr2dbc.entities.ReactiveComments;
import com.example.bootr2dbc.repositories.ReactiveCommentsRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ReactiveCommentsServiceTest {

    @Mock
    private ReactiveCommentsRepository reactiveCommentsRepository;

    @InjectMocks
    private ReactiveCommentsService reactiveCommentsService;

    @Test
    void findAllReactiveCommentss() {
        // given
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        given(reactiveCommentsRepository.findAllByPostId(1L, sort)).willReturn(Flux.just(getReactiveComments()));

        // when
        Flux<ReactiveComments> pagedResult = reactiveCommentsService.findAllReactiveCommentsByPostId(1L, "id", "asc");

        // then
        assertThat(pagedResult).isNotNull();
        StepVerifier.create(pagedResult)
                .expectNextMatches(reactivePostComment -> reactivePostComment
                                .getPostId()
                                .equals(getReactiveComments().getPostId())
                        && reactivePostComment
                                .getContent()
                                .equals(getReactiveComments().getContent()))
                .expectComplete()
                .verify();
    }

    @Test
    void findReactiveCommentsById() {
        // given
        UUID postCommentId = UUID.randomUUID();
        given(reactiveCommentsRepository.findById(postCommentId)).willReturn(Mono.just(getReactiveComments()));
        // when
        Mono<ReactiveComments> reactiveCommentsMono = reactiveCommentsService.findReactiveCommentsById(postCommentId);
        // then
        StepVerifier.create(reactiveCommentsMono)
                .expectNextMatches(reactivePostComment -> reactivePostComment
                                .getPostId()
                                .equals(getReactiveComments().getPostId())
                        && reactivePostComment
                                .getContent()
                                .equals(getReactiveComments().getContent()))
                .expectComplete()
                .verify();
    }

    // @Test
    // void saveReactiveComments() {
    //     // given
    //     given(reactiveCommentsRepository.save(getReactiveComments())).willReturn(getReactiveComments());
    //     // when
    //     ReactiveComments persistedReactiveComments =
    //             reactiveCommentsService.saveReactiveComments(getReactiveComments());
    //     // then
    //     assertThat(persistedReactiveComments).isNotNull();
    //     assertThat(persistedReactiveComments.getId()).isEqualTo(1L);
    //     assertThat(persistedReactiveComments.getText()).isEqualTo("junitTest");
    // }

    // @Test
    // void deleteReactiveCommentsById() {
    //     // given
    //     willDoNothing().given(reactiveCommentsRepository).deleteById(1L);
    //     // when
    //     reactiveCommentsService.deleteReactiveCommentsById(1L);
    //     // then
    //     verify(reactiveCommentsRepository, times(1)).deleteById(1L);
    // }

    private ReactiveComments getReactiveComments() {
        ReactiveComments reactiveComments = new ReactiveComments();
        reactiveComments.setId(UUID.randomUUID());
        reactiveComments.setContent("junitContent");
        reactiveComments.setPostId(1L);
        return reactiveComments;
    }
}
