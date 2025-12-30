package com.example.graphql.gql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.graphql.execution.ErrorType.NOT_FOUND;

import com.example.graphql.config.graphql.GraphQlConfiguration;
import com.example.graphql.model.response.AuthorResponse;
import com.example.graphql.services.AuthorService;
import com.example.graphql.services.PostCommentService;
import com.example.graphql.services.PostService;
import com.example.graphql.services.TagService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@GraphQlTest(AuthorGraphQlController.class)
@Import(GraphQlConfiguration.class)
class AuthorEntityQueryTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    AuthorService authorService;

    @MockitoBean
    PostService postService;

    @MockitoBean
    PostCommentService postCommentService;

    @MockitoBean
    TagService tagService;

    @Test
    void allAuthors() {
        given(authorService.findAllAuthors())
                .willReturn(List.of(
                        new AuthorResponse(
                                1L,
                                "firstName",
                                "middleName",
                                "lastName",
                                9848022338L,
                                "junit1@email.com",
                                LocalDateTime.now()),
                        new AuthorResponse(
                                2L,
                                "secondName",
                                "middleName",
                                "lastName",
                                9848022338L,
                                "junit2@email.com",
                                LocalDateTime.now())));

        var allAuthors = """
                query authorEntities{
                    allAuthors{
                     id
                     firstName
                     email
                   }
                 }""";
        graphQlTester
                .document(allAuthors)
                .execute()
                .path("allAuthors[*].email")
                .entityList(String.class)
                .satisfies(emails -> assertThat(emails).contains("junit1@email.com", "junit2@email.com"))
                .path("allAuthors[*].firstName")
                .entityList(String.class)
                .satisfies(names -> assertThat(names).containsAll(List.of("firstName", "secondName")));

        verify(authorService, times(1)).findAllAuthors();
        verifyNoMoreInteractions(authorService);
    }

    @Test
    void findAuthorByEmailId() {
        given(authorService.findAuthorByEmailId("junit@email.com")).willReturn(Optional.empty());
        graphQlTester
                .documentName("findAuthorByEmail")
                .variable("emailId", "junit@email.com")
                .execute()
                .errors()
                .satisfy(responseErrors -> {
                    assertThat(responseErrors).hasSize(1);
                    assertThat(responseErrors.getFirst().getPath()).isEqualTo("findAuthorByEmailId");
                    assertThat(responseErrors.getFirst().getErrorType()).isEqualTo(NOT_FOUND);
                    assertThat(responseErrors.getFirst().getMessage())
                            .isEqualTo("Author: junit@email.com was not found.");
                });

        verify(authorService, times(1)).findAuthorByEmailId("junit@email.com");
        verifyNoMoreInteractions(authorService);
    }
}
