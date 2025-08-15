package com.example.graphql.gql;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.graphql.config.graphql.GraphQlConfiguration;
import com.example.graphql.model.request.PostCommentRequest;
import com.example.graphql.model.response.PostCommentResponse;
import com.example.graphql.services.PostCommentService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@GraphQlTest(PostCommentsQlController.class)
@Import(GraphQlConfiguration.class)
class PostCommentsQlControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    PostCommentService postCommentService;

    @Test
    void addCommentToPost() {
        var requestMap = Map.of(
                "title", "Title",
                "content", "Content",
                "postId", "1",
                "published", true);
        PostCommentRequest expectedRequest = new PostCommentRequest("Title", "Content", "1", true);
        PostCommentResponse response = PostCommentResponse.builder()
                .title("Title")
                .content("Content")
                .postId(1L)
                .commentId(100L)
                .published(true)
                .build();
        given(postCommentService.addCommentToPost(expectedRequest)).willReturn(response);

        var mutation =
                """
            mutation addCommentToPost($addCommentToPostRequest: AddCommentToPostRequest!) {
                addCommentToPost(addCommentToPostRequest: $addCommentToPostRequest) {
                    title
                }
            }
        """;
        graphQlTester
                .document(mutation)
                .variable("addCommentToPostRequest", requestMap)
                .execute()
                .path("addCommentToPost.title")
                .entity(String.class)
                .isEqualTo("Title");

        verify(postCommentService, times(1)).addCommentToPost(expectedRequest);
        verifyNoMoreInteractions(postCommentService);
    }
}
