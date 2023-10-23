package com.example.rest.proxy.services;

import com.example.rest.proxy.client.JsonPlaceholderService;
import com.example.rest.proxy.entities.Post;
import com.example.rest.proxy.exception.PostNotFoundException;
import com.example.rest.proxy.model.response.PagedResult;
import com.example.rest.proxy.repositories.PostRepository;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final JsonPlaceholderService jsonPlaceholderService;

    public PagedResult<Post> findAllPosts(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort =
                sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Post> postsPage = postRepository.findAll(pageable);

        return new PagedResult<>(postsPage);
    }

    public Optional<Post> findPostById(Long id) {
        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isPresent()) {
            return optionalPost;
        } else {
            Function<Long, Post> loadPostById = jsonPlaceholderService::loadPostById;
            return Optional.of(callService(id, loadPostById.andThen(this::savePost)));
        }
    }

    public Post savePost(Post post) {
        Post savedPost = jsonPlaceholderService.createPost(post);
        return postRepository.save(savedPost);
    }

    public void deletePostById(Long id) {
        jsonPlaceholderService.deletePostById(id);
        postRepository.deleteById(id);
    }

    private <T> T callService(Long id, Function<Long, T> serviceFunction) {
        T result;
        try {
            result = serviceFunction.apply(id);
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PostNotFoundException(id);
            }
            throw exception;
        }
        return result;
    }
}
