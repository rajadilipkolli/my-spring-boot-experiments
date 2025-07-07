package com.example.graphql.services;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.entities.PostCommentEntity;
import com.example.graphql.mapper.PostCommentEntityToResponseMapper;
import com.example.graphql.mapper.PostCommentRequestToEntityMapper;
import com.example.graphql.model.request.PostCommentRequest;
import com.example.graphql.model.response.PostCommentResponse;
import com.example.graphql.repositories.PostCommentRepository;
import com.example.graphql.repositories.PostRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Loggable
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final PostCommentEntityToResponseMapper postCommentEntityToResponseMapper;
    private final PostCommentRequestToEntityMapper postCommentRequestToEntityMapper;

    public List<PostCommentResponse> findAllPostComments() {
        List<CompletableFuture<PostCommentResponse>> completableFutureList = postCommentRepository.findAll().stream()
                .map(postCommentEntity -> CompletableFuture.supplyAsync(
                        () -> postCommentEntityToResponseMapper.convert(postCommentEntity)))
                .toList();
        return completableFutureList.stream().map(CompletableFuture::join).toList();
    }

    public Optional<PostCommentResponse> findPostCommentById(Long id) {
        return this.postCommentRepository.findById(id).map(postCommentEntityToResponseMapper::convert);
    }

    public Optional<PostCommentEntity> findCommentById(Long commentId) {
        return this.postCommentRepository.findById(commentId);
    }

    @Transactional
    public PostCommentResponse addCommentToPost(PostCommentRequest postCommentRequest) {
        PostCommentEntity postCommentEntity =
                postCommentRequestToEntityMapper.covert(postCommentRequest, postRepository);
        return saveAndConvert(postCommentEntity);
    }

    @Transactional
    public void deletePostCommentById(Long id) {
        postCommentRepository.deleteById(id);
    }

    public Map<Long, List<PostCommentResponse>> getCommentsByPostIdIn(List<Long> postIds) {
        return this.postCommentRepository.findByPostEntity_IdIn(postIds).stream()
                .map(postCommentEntityToResponseMapper::convert)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(PostCommentResponse::postId));
    }

    @Transactional
    public PostCommentResponse updatePostComment(
            PostCommentEntity postCommentEntity, PostCommentRequest postCommentRequest) {
        postCommentEntityToResponseMapper.updatePostCommentEntity(postCommentRequest, postCommentEntity);
        // if published is changed to true then publishedAt should be set
        if (postCommentEntity.isPublished() && postCommentEntity.getPublishedAt() == null) {
            postCommentEntity.setPublishedAt(OffsetDateTime.now());
        }
        return saveAndConvert(postCommentEntity);
    }

    private PostCommentResponse saveAndConvert(PostCommentEntity postCommentEntity) {
        PostCommentEntity persistedPostComment = postCommentRepository.save(postCommentEntity);
        return postCommentEntityToResponseMapper.convert(persistedPostComment);
    }
}
