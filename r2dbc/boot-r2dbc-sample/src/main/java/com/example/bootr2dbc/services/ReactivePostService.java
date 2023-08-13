package com.example.bootr2dbc.services;

import com.example.bootr2dbc.entities.ReactivePost;
import com.example.bootr2dbc.model.response.PagedResult;
import com.example.bootr2dbc.repositories.ReactivePostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class ReactivePostService {

    private final ReactivePostRepository reactivePostRepository;

    @Autowired
    public ReactivePostService(ReactivePostRepository reactivePostRepository) {
        this.reactivePostRepository = reactivePostRepository;
    }

    public PagedResult<ReactivePost> findAllReactivePosts(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<ReactivePost> reactivePostsPage = reactivePostRepository.findAll(pageable);

        return new PagedResult<>(reactivePostsPage);
    }

    public Mono<ReactivePost> findReactivePostById(Long id) {
        return reactivePostRepository.findById(id);
    }

    public Mono<ReactivePost> saveReactivePost(ReactivePost reactivePost) {
        return reactivePostRepository.save(reactivePost);
    }

    public void deleteReactivePostById(Long id) {
        reactivePostRepository.deleteById(id);
    }
}
