package com.example.graphql.querydsl.mapper;

import com.example.graphql.querydsl.entities.PostComment;
import com.example.graphql.querydsl.model.request.PostCommentRequest;
import com.example.graphql.querydsl.model.response.PostCommentResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PostCommentMapper {

    public PostComment toEntity(PostCommentRequest postCommentRequest) {
        PostComment postComment = new PostComment();
        postComment.setText(postCommentRequest.text());
        return postComment;
    }

    public void mapPostCommentWithRequest(PostComment postComment, PostCommentRequest postCommentRequest) {
        postComment.setText(postCommentRequest.text());
    }

    public PostCommentResponse toResponse(PostComment postComment) {
        return new PostCommentResponse(postComment.getId(), postComment.getText());
    }

    public List<PostCommentResponse> toResponseList(List<PostComment> postCommentList) {
        return postCommentList.stream().map(this::toResponse).toList();
    }
}
