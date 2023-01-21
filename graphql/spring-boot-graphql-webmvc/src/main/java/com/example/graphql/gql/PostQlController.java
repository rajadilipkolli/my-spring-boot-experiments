package com.example.graphql.gql;

import com.example.graphql.projections.PostInfo;
import com.example.graphql.services.PostService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@Validated
@RequiredArgsConstructor
public class PostQlController {

    private final PostService postService;

    @QueryMapping
    public List<PostInfo> allPostsByEmail(@Argument("email") String email) {
        return this.postService.findAllPostsByAuthorEmail(email);
    }
}
