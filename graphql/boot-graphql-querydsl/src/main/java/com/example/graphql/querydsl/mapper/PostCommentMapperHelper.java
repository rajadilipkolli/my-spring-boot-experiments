package com.example.graphql.querydsl.mapper;

import com.example.graphql.querydsl.entities.PostComment;
import com.example.graphql.querydsl.model.request.CreatePostCommentRequest;
import com.example.graphql.querydsl.repositories.PostRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Service;

@Service
class PostCommentMapperHelper {

    private final PostRepository postRepository;

    public PostCommentMapperHelper(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @AfterMapping
    void setPost(CreatePostCommentRequest createPostCommentRequest, @MappingTarget PostComment postComment) {
        postComment.setPost(postRepository.getReferenceById(createPostCommentRequest.postId()));
    }
}
