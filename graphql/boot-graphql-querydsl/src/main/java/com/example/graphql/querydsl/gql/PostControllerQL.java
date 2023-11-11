package com.example.graphql.querydsl.gql;

import com.example.graphql.querydsl.model.request.AddTagRequest;
import com.example.graphql.querydsl.model.request.CreatePostRequest;
import com.example.graphql.querydsl.model.response.PostResponse;
import com.example.graphql.querydsl.services.PostService;
import java.util.List;
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

    @QueryMapping
    public List<PostResponse> getPostsByUserName(@Argument("name") String name) {
        return this.postService.getPostsByUserName(name);
    }

    @MutationMapping
    public PostResponse createPost(@Argument("createPostRequest") CreatePostRequest createPostRequest) {
        return this.postService.savePost(createPostRequest);
    }

    @MutationMapping
    public PostResponse addTagsToPost(@Argument("addTagRequest") AddTagRequest addTagRequest) {
        return this.postService.addTagsToPost(addTagRequest);
    }
}
