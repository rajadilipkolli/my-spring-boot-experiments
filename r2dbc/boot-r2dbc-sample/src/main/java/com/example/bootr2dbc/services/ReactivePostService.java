package com.example.bootr2dbc.services;

import com.example.bootr2dbc.entities.ReactiveComments;
import com.example.bootr2dbc.entities.ReactivePost;
import com.example.bootr2dbc.mapper.ReactivePostMapper;
import com.example.bootr2dbc.model.ReactivePostRequest;
import com.example.bootr2dbc.repositories.ReactiveCommentsRepository;
import com.example.bootr2dbc.repositories.ReactivePostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional
@RequiredArgsConstructor
public class ReactivePostService {

    private final ReactivePostRepository reactivePostRepository;
    private final ReactiveCommentsRepository reactiveCommentsRepository;
    private final ReactivePostMapper reactivePostMapper;

    @Transactional(readOnly = true)
    public Flux<ReactivePost> findAllReactivePosts(String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        return reactivePostRepository.findAll(sort);
    }

    @Transactional(readOnly = true)
    public Mono<ReactivePost> findReactivePostById(Long id) {
        return reactivePostRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Flux<ReactiveComments> findCommentsForReactivePost(Long id) {
        return reactiveCommentsRepository.findAllByPostId(id);
    }

    public Mono<ReactivePost> saveReactivePost(ReactivePostRequest reactivePostRequest) {
        ReactivePost reactivePost = reactivePostMapper.mapToReactivePost(reactivePostRequest);
        return reactivePostRepository.save(reactivePost);
    }

    public Mono<ReactivePost> updateReactivePost(
            ReactivePostRequest reactivePostRequest, ReactivePost reactivePost) {
        this.reactivePostMapper.updateReactivePostFromReactivePostRequest(
                reactivePostRequest, reactivePost);
        return reactivePostRepository.save(reactivePost);
    }

    public Mono<Void> deleteReactivePostById(Long id) {
        return reactivePostRepository.deleteById(id);
    }

    public Mono<ResponseEntity<Object>> deleteReactivePostAndCommentsById(Long id) {
        return findReactivePostById(id)
                .flatMap(
                        reactivePost ->
                                reactiveCommentsRepository
                                        .deleteAllByPostId(reactivePost.getId())
                                        .then(deleteReactivePostById(reactivePost.getId()))
                                        .then(Mono.just(ResponseEntity.noContent().build())))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
