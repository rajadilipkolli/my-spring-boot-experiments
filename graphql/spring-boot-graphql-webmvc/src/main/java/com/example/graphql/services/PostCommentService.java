package com.example.graphql.services;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.entities.PostCommentEntity;
import com.example.graphql.mapper.PostCommentEntityToResponseMapper;
import com.example.graphql.model.request.PostCommentRequest;
import com.example.graphql.model.response.PostCommentResponse;
import com.example.graphql.repositories.PostCommentRepository;
import com.example.graphql.repositories.PostRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Loggable
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final PostCommentEntityToResponseMapper postCommentEntityToResponseMapper;

    @Transactional(readOnly = true)
    public List<PostCommentResponse> findAllPostComments() {
        List<CompletableFuture<PostCommentResponse>> completableFutureList =
                postCommentRepository.findAll().stream()
                        .map(
                                postCommentEntity ->
                                        CompletableFuture.supplyAsync(
                                                () ->
                                                        postCommentEntityToResponseMapper.convert(
                                                                postCommentEntity)))
                        .toList();
        return completableFutureList.stream().map(CompletableFuture::join).toList();
    }

    @Transactional(readOnly = true)
    public Optional<PostCommentResponse> findPostCommentById(Long id) {
        return postCommentRepository.findById(id).map(postCommentEntityToResponseMapper::convert);
    }

    @Transactional(readOnly = true)
    public Optional<PostCommentEntity> findCommentById(Long commentId) {
        return this.postCommentRepository.findById(commentId);
    }

    public PostCommentResponse addCommentToPost(PostCommentRequest postCommentRequest) {
        PostCommentEntity postCommentEntity =
                PostCommentEntity.builder()
                        .postEntity(postRepository.getReferenceById(postCommentRequest.postId()))
                        .title(postCommentRequest.title())
                        .content(postCommentRequest.content())
                        .published(postCommentRequest.published())
                        .build();
        if (postCommentEntity.isPublished()) {
            postCommentEntity.setPublishedAt(LocalDateTime.now());
        }
        return saveAndConvert(postCommentEntity);
    }

    public void deletePostCommentById(Long id) {
        postCommentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<PostCommentEntity>> getCommentsByPostIdIn(List<Long> postIds) {
        return this.postCommentRepository.findByPostEntity_IdIn(postIds).stream()
                .collect(Collectors.groupingBy(postComment -> postComment.getPostEntity().getId()));
    }

    public PostCommentResponse updatePostComment(
            PostCommentEntity postCommentEntity, PostCommentRequest postCommentRequest) {
        postCommentEntityToResponseMapper.updatePostCommentEntity(
                postCommentRequest, postCommentEntity);
        // if published is changed to true then publishedAt should be set
        if (postCommentEntity.isPublished() && postCommentEntity.getPublishedAt() == null) {
            postCommentEntity.setPublishedAt(LocalDateTime.now());
        }
        return saveAndConvert(postCommentEntity);
    }

    private PostCommentResponse saveAndConvert(PostCommentEntity postCommentEntity) {
        PostCommentEntity persistedPostComment = postCommentRepository.save(postCommentEntity);
        return postCommentEntityToResponseMapper.convert(persistedPostComment);
    }
}
