package com.example.graphql.gql;

import com.example.graphql.model.request.PostCommentRequest;
import com.example.graphql.model.response.PostCommentResponse;
import com.example.graphql.services.PostCommentService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@Validated
public class PostCommentsQlController {

    private final PostCommentService postCommentService;

    @MutationMapping
    public PostCommentResponse addCommentToPost(
            @Valid @Argument("addCommentToPostRequest") PostCommentRequest postCommentRequest) {
        return this.postCommentService.addCommentToPost(postCommentRequest);
    }
}
