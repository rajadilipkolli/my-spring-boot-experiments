package com.example.graphql.querydsl.gql;

import com.example.graphql.querydsl.model.request.CreatePostRequest;
import com.example.graphql.querydsl.model.response.PostResponse;
import com.example.graphql.querydsl.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PostControllerQL {

    private final PostService postService;

    @QueryMapping
    public Long countPosts() {
        return this.postService.totalPosts();
    }

    @MutationMapping
    public PostResponse createPost(@Argument("createPostRequest") CreatePostRequest createPostRequest) {
        return this.postService.savePost(createPostRequest);
    }
}
