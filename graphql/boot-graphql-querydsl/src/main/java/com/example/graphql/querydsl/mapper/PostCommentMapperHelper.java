package com.example.graphql.querydsl.mapper;

import com.example.graphql.querydsl.entities.PostComment;
import com.example.graphql.querydsl.model.request.CreatePostCommentRequest;
import com.example.graphql.querydsl.repositories.PostRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper
public abstract class PostCommentMapperHelper {

    @Autowired
    private PostRepository postRepository;

    @AfterMapping
    void setPost(CreatePostCommentRequest createPostCommentRequest, @MappingTarget PostComment postComment) {
        postComment.setPost(postRepository.getReferenceById(createPostCommentRequest.postId()));
    }
}
