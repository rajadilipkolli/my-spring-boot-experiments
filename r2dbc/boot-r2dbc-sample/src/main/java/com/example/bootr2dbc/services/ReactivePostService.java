package com.example.bootr2dbc.services;

import com.example.bootr2dbc.entities.ReactivePost;
import com.example.bootr2dbc.model.ReactivePostRequest;
import com.example.bootr2dbc.repositories.ReactivePostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class ReactivePostService {

    private final ReactivePostRepository reactivePostRepository;

    @Autowired
    public ReactivePostService(ReactivePostRepository reactivePostRepository) {
        this.reactivePostRepository = reactivePostRepository;
    }

    public Flux<ReactivePost> findAllReactivePosts(String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return reactivePostRepository.findAll(sort);
    }

    public Mono<ReactivePost> findReactivePostById(Long id) {
        return reactivePostRepository.findById(id);
    }

    public Mono<ReactivePost> saveReactivePost(ReactivePostRequest reactivePostRequest) {
        ReactivePost reactivePost = mapToReactivePost(reactivePostRequest);
        return reactivePostRepository.save(reactivePost);
    }

    public Mono<ReactivePost> updateReactivePost(ReactivePostRequest reactivePostRequest, Long id) {
        ReactivePost reactivePost = mapToReactivePost(reactivePostRequest);
        reactivePost.setId(id);
        return reactivePostRepository.save(reactivePost);
    }

    private ReactivePost mapToReactivePost(ReactivePostRequest reactivePostRequest) {
        return ReactivePost.builder()
                .content(reactivePostRequest.content())
                .title(reactivePostRequest.title())
                .build();
    }

    public Mono<Void> deleteReactivePostById(Long id) {
        return reactivePostRepository.deleteById(id);
    }
}
