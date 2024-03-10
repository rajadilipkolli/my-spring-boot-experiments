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
    private final PostCommentRepository postCommentRepository;
    private final JsonPlaceholderService jsonPlaceholderService;
    private final PostMapper postMapper;

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

    public Optional<PostResponse> findPostById(Long postId) {
        Post post =
                postRepository
                        .findById(postId)
                        .orElseGet(
                                () -> {
                                    Function<Long, Post> loadPostById =
                                            jsonPlaceholderService::loadPostById;
                                    return callService(postId, loadPostById.andThen(this::save));
                                });

        return Optional.of(postMapper.mapToPostResponse(post));
    }

    public Optional<List<PostCommentDto>> findPostCommentsById(Long postId) {
        List<PostComment> postCommentList = postCommentRepository.findByPostId(postId);
        if (postCommentList.isEmpty()) {
            Function<Long, List<PostCommentDto>> loadPostById =
                    jsonPlaceholderService::loadPostCommentsById;
            postCommentList = callService(postId, loadPostById.andThen(this::savePostComments));
        }
        return Optional.of(postMapper.mapToResponseList(postCommentList));
    }

    private List<PostComment> savePostComments(List<PostCommentDto> postCommentDtos) {

        return postCommentRepository.saveAll(
                postMapper.mapToEntityList(postCommentDtos, postRepository));
    }

    public PostResponse savePost(Post post) {
        Post fetchedPost = jsonPlaceholderService.createPost(post);
        Post savedPost = postRepository.save(fetchedPost);
        return postMapper.mapToPostResponse(savedPost);
    }

    public Post save(Post postEntity) {
        return postRepository.save(postEntity);
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
