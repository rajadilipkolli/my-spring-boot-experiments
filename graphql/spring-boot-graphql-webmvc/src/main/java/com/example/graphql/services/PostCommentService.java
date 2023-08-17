package com.example.graphql.services;

import com.example.graphql.config.logging.Loggable;
import com.example.graphql.entities.PostCommentEntity;
import com.example.graphql.model.request.AddCommentToPostRequest;
import com.example.graphql.repositories.PostCommentRepository;
import com.example.graphql.repositories.PostRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Transactional(readOnly = true)
    public List<PostCommentEntity> findAllPostComments() {
        return postCommentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<PostCommentEntity> findPostCommentById(Long id) {
        return postCommentRepository.findById(id);
    }

    public PostCommentEntity savePostComment(PostCommentEntity postCommentEntity) {
        return postCommentRepository.save(postCommentEntity);
    }

    public PostCommentEntity addCommentToPost(AddCommentToPostRequest addCommentToPostRequest) {
        PostCommentEntity postCommentEntity =
                PostCommentEntity.builder()
                        .postEntity(
                                postRepository.getReferenceById(addCommentToPostRequest.postId()))
                        .title(addCommentToPostRequest.title())
                        .content(addCommentToPostRequest.content())
                        .published(addCommentToPostRequest.published())
                        .build();
        if (postCommentEntity.isPublished()) {
            postCommentEntity.setPublishedAt(LocalDateTime.now());
        }
        return postCommentRepository.save(postCommentEntity);
    }

    public void deletePostCommentById(Long id) {
        postCommentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<PostCommentEntity>> getCommentsByPostIdIn(List<Long> postIds) {
        return this.postCommentRepository.findByPostEntity_IdIn(postIds).stream()
                .collect(Collectors.groupingBy(postComment -> postComment.getPostEntity().getId()));
    }
}
