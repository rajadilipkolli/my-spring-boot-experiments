package com.example.graphql.gql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.graphql.config.graphql.GraphQlConfiguration;
import com.example.graphql.entities.PostEntity;
import com.example.graphql.model.request.NewPostRequest;
import com.example.graphql.projections.AuthorInfo;
import com.example.graphql.projections.PostDetailsInfo;
import com.example.graphql.projections.PostInfo;
import com.example.graphql.services.PostService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@GraphQlTest(PostQlController.class)
@Import(GraphQlConfiguration.class)
class PostQlControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    PostService postService;

    record TestPostInfo(Long id, String title, String content, boolean published) implements PostInfo {
        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getContent() {
            return content;
        }

        @Override
        public boolean isPublished() {
            return published;
        }

        public LocalDateTime getCreatedAt() {
            return LocalDateTime.of(2024, 1, 1, 10, 0);
        }

        public LocalDateTime getModifiedAt() {
            return LocalDateTime.of(2024, 1, 1, 11, 0, 0);
        }

        public LocalDateTime getPublishedAt() {
            return LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        }

        public PostDetailsInfo getDetails() {
            return null;
        }

        public AuthorInfo getAuthorEntity() {
            return null;
        }
    }

    @Test
    void allPostsByEmail() {
        given(postService.findAllPostsByAuthorEmail("test@email.com"))
                .willReturn(List.of(
                        new TestPostInfo(1L, "Title1", "Content1", true),
                        new TestPostInfo(2L, "Title2", "Content2", false)));

        var query =
                """
            query allPostsByEmail($email: String!) {
                allPostsByEmail(email: $email) {
                    id
                    title
                    content
                    published
                }
            }
        """;
        graphQlTester
                .document(query)
                .variable("email", "test@email.com")
                .execute()
                .path("allPostsByEmail[*].title")
                .entityList(String.class)
                .satisfies(titles -> assertThat(titles).contains("Title1", "Title2"));

        verify(postService, times(1)).findAllPostsByAuthorEmail("test@email.com");
        verifyNoMoreInteractions(postService);
    }

    @Test
    void createPost() {
        Map<String, Object> inputValues = new HashMap<>();
        inputValues.put("title", "JunitTitle");
        inputValues.put("content", "JunitContent");
        inputValues.put("email", "Junit@email.com");
        inputValues.put("published", true);
        Map<String, String> detailsValues = new HashMap<>();
        detailsValues.put("detailsKey", "newPost");
        inputValues.put("details", detailsValues);
        PostEntity expectedPost = new PostEntity();
        expectedPost.setTitle("JunitTitle");
        expectedPost.setId(2L);
        expectedPost.setContent("JunitContent");
        given(postService.createPost(any(NewPostRequest.class))).willReturn(expectedPost);

        graphQlTester
                .documentName("createPost")
                .variable("newPostRequest", inputValues)
                .execute()
                .path("createPost.title")
                .entity(String.class)
                .isEqualTo("JunitTitle");

        verify(postService, times(1)).createPost(any(NewPostRequest.class));
        verifyNoMoreInteractions(postService);
    }
}
