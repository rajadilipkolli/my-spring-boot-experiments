package com.example.graphql.gql;

import com.example.graphql.entities.PostCommentEntity;
import com.example.graphql.model.request.AddCommentToPostRequest;
import com.example.graphql.services.PostCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@Validated
@RequiredArgsConstructor
public class PostCommentsQlController {

    private final PostCommentService postCommentService;

    @MutationMapping
    public PostCommentEntity addCommentToPost(
            @Valid @Argument("addCommentToPostRequest")
                    AddCommentToPostRequest addCommentToPostRequest) {
        return this.postCommentService.addCommentToPost(addCommentToPostRequest);
    }
}
