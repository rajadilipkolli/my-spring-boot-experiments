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
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Loggable
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final ConversionService appConversionService;
    private final PostCommentEntityToResponseMapper postCommentEntityToResponseMapper;
    private final PostCommentRequestToEntityMapper postCommentRequestToEntityMapper;

    public PostCommentService(
            PostCommentRepository postCommentRepository,
            PostRepository postRepository,
            ConversionService appConversionService,
            PostCommentEntityToResponseMapper postCommentEntityToResponseMapper,
            PostCommentRequestToEntityMapper postCommentRequestToEntityMapper) {
        this.postCommentRepository = postCommentRepository;
        this.postRepository = postRepository;
        this.appConversionService = appConversionService;
        this.postCommentEntityToResponseMapper = postCommentEntityToResponseMapper;
        this.postCommentRequestToEntityMapper = postCommentRequestToEntityMapper;
    }

    public List<PostCommentResponse> findAllPostComments() {
        List<CompletableFuture<PostCommentResponse>> completableFutureList = postCommentRepository.findAll().stream()
                .map(postCommentEntity -> CompletableFuture.supplyAsync(
                        () -> appConversionService.convert(postCommentEntity, PostCommentResponse.class)))
                .toList();
        return completableFutureList.stream().map(CompletableFuture::join).toList();
    }

    public Optional<PostCommentResponse> findPostCommentById(Long id) {
        return this.postCommentRepository
                .findById(id)
                .map(postCommentEntity -> appConversionService.convert(postCommentEntity, PostCommentResponse.class));
    }

    public Optional<PostCommentEntity> findCommentById(Long commentId) {
        return this.postCommentRepository.findById(commentId);
    }

    @Transactional
    public PostCommentResponse addCommentToPost(PostCommentRequest postCommentRequest) {
        PostCommentEntity postCommentEntity =
                postCommentRequestToEntityMapper.convert(postCommentRequest, postRepository);
        return saveAndConvert(postCommentEntity);
    }

    @Transactional
    public void deletePostCommentById(Long id) {
        postCommentRepository.deleteById(id);
    }

    public Map<Long, List<PostCommentResponse>> getCommentsByPostIdIn(List<Long> postIds) {
        return this.postCommentRepository.findByPostEntity_IdIn(postIds).stream()
                .map(postCommentEntity -> appConversionService.convert(postCommentEntity, PostCommentResponse.class))
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
        return appConversionService.convert(persistedPostComment, PostCommentResponse.class);
    }
}
