package com.example.graphql.services;

import com.example.graphql.entities.PostComment;
import com.example.graphql.repositories.PostCommentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;

    @Autowired
    public PostCommentService(PostCommentRepository postCommentRepository) {
        this.postCommentRepository = postCommentRepository;
    }

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
}
