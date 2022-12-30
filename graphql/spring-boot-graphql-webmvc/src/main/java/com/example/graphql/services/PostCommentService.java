package com.example.graphql.services;

import com.example.graphql.entities.PostComment;
import com.example.graphql.repositories.PostCommentRepository;
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
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;

    public List<PostComment> findAllPostComments() {
        return postCommentRepository.findAll();
    }

    public Optional<PostComment> findPostCommentById(Long id) {
        return postCommentRepository.findById(id);
    }

    public PostComment savePostComment(PostComment postComment) {
        return postCommentRepository.save(postComment);
    }

    public void deletePostCommentById(Long id) {
        postCommentRepository.deleteById(id);
    }

    public Map<Long, List<PostComment>> getCommentsByPostIdIn(List<Long> postIds) {
        return this.postCommentRepository.findByPost_IdIn(postIds).stream()
                .collect(Collectors.groupingBy(postComment -> postComment.getPost().getId()));
    }
}
