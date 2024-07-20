package com.example.graphql.querydsl.gql;

import com.example.graphql.querydsl.model.request.AddTagRequest;
import com.example.graphql.querydsl.model.request.CreatePostRequest;
import com.example.graphql.querydsl.model.response.PostResponse;
import com.example.graphql.querydsl.services.PostService;
import java.util.List;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
class PostControllerQL {

    private final PostService postService;

    PostControllerQL(PostService postService) {
        this.postService = postService;
    }

    @QueryMapping
    Long countPosts() {
        return this.postService.totalPosts();
    }

    @QueryMapping
    List<PostResponse> getPostsByUserName(@Argument("name") String name) {
        return this.postService.getPostsByUserName(name);
    }

    @MutationMapping
    PostResponse createPost(@Argument("createPostRequest") CreatePostRequest createPostRequest) {
        return this.postService.savePost(createPostRequest);
    }

    @MutationMapping
    PostResponse addTagsToPost(@Argument("addTagRequest") AddTagRequest addTagRequest) {
        return this.postService.addTagsToPost(addTagRequest);
    }
}
