package com.example.graphql.querydsl.gql;

import com.example.graphql.querydsl.services.PostService;
import lombok.RequiredArgsConstructor;
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
}
