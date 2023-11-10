package com.example.graphql.querydsl.mapper;

import com.example.graphql.querydsl.entities.Post;
import com.example.graphql.querydsl.model.request.PostRequest;
import com.example.graphql.querydsl.model.response.PostResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PostMapper {

    public Post toEntity(PostRequest postRequest) {
        Post post = new Post();
        post.setText(postRequest.text());
        return post;
    }

    public void mapPostWithRequest(Post post, PostRequest postRequest) {
        post.setText(postRequest.text());
    }

    public PostResponse toResponse(Post post) {
        return new PostResponse(post.getId(), post.getText());
    }

    public List<PostResponse> toResponseList(List<Post> postList) {
        return postList.stream().map(this::toResponse).toList();
    }
}
