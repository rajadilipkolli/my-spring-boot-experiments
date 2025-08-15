package com.example.rest.proxy.services;

import com.example.rest.proxy.client.JsonPlaceholderService;
import com.example.rest.proxy.entities.Post;
import com.example.rest.proxy.entities.PostComment;
import com.example.rest.proxy.exception.PostNotFoundException;
import com.example.rest.proxy.mapper.PostMapper;
import com.example.rest.proxy.model.response.PagedResult;
import com.example.rest.proxy.model.response.PostCommentDto;
import com.example.rest.proxy.model.response.PostResponse;
import com.example.rest.proxy.repositories.PostCommentRepository;
import com.example.rest.proxy.repositories.PostRepository;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final JsonPlaceholderService jsonPlaceholderService;
    private final PostMapper postMapper;

    public PostService(
            PostRepository postRepository,
            PostCommentRepository postCommentRepository,
            JsonPlaceholderService jsonPlaceholderService,
            PostMapper postMapper) {
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.jsonPlaceholderService = jsonPlaceholderService;
        this.postMapper = postMapper;
    }

    public PagedResult<PostResponse> findAllPosts(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Post> page = postRepository.findAll(pageable);

        List<PostResponse> postResponses = postMapper.mapToPostResponseList(page.getContent());

        return new PagedResult<>(
                postResponses,
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious());
    }

    @Transactional
    public Optional<PostResponse> findPostById(Long postId) {
        Post post = postRepository.findById(postId).orElseGet(() -> {
            Function<Long, Post> loadPostById = jsonPlaceholderService::loadPostById;
            return callService(postId, loadPostById.andThen(this::save));
        });

        return Optional.of(postMapper.mapToPostResponse(post));
    }

    public Optional<List<PostCommentDto>> findPostCommentsById(Long postId) {
        List<PostComment> postCommentList = postCommentRepository.findByPostId(postId);
        if (postCommentList.isEmpty()) {
            Function<Long, List<PostCommentDto>> loadPostById = jsonPlaceholderService::loadPostCommentsById;
            postCommentList = callService(postId, loadPostById.andThen(this::savePostComments));
        }
        return Optional.of(postMapper.mapToCommentResponseList(postCommentList));
    }

    @Transactional
    public List<PostComment> savePostComments(List<PostCommentDto> postCommentDtoList) {
        return postCommentRepository.saveAll(postMapper.mapToEntityList(postCommentDtoList, postRepository));
    }

    @Transactional
    public PostResponse savePost(Post post) {
        Post fetchedPost = jsonPlaceholderService.createPost(post);
        // To fix optimisticLock Exception
        // Create a new local entity from the external post
        Post localPost = new Post();
        localPost.setUserId(fetchedPost.getUserId());
        localPost.setTitle(fetchedPost.getTitle());
        localPost.setBody(fetchedPost.getBody());
        Post savedPost = postRepository.save(localPost);
        return postMapper.mapToPostResponse(savedPost);
    }

    @Transactional
    public Post save(Post postEntity) {
        // Create a new local entity from the external post
        Post localPost = new Post();
        localPost.setUserId(postEntity.getUserId());
        localPost.setTitle(postEntity.getTitle());
        localPost.setBody(postEntity.getBody());
        return postRepository.save(localPost);
    }

    @Transactional
    public PostResponse saveAndConvertToResponse(Post post) {
        Post savedPost = postRepository.save(post);
        return postMapper.mapToPostResponse(savedPost);
    }

    @Transactional
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
