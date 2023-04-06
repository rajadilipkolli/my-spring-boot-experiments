package com.example.graphql.gql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.graphql.model.response.AuthorResponse;
import com.example.graphql.services.AuthorService;
import com.example.graphql.services.PostCommentService;
import com.example.graphql.services.PostService;
import com.example.graphql.services.TagService;

import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.LocalDateTime;
import java.util.List;

@GraphQlTest(AuthorGraphQlController.class)
class AuthorEntityQueryTest {

    @Autowired GraphQlTester graphQlTester;

    @MockBean AuthorService authorService;
    @MockBean PostService postService;
    @MockBean PostCommentService postCommentService;
    @MockBean TagService tagService;

    @Test
    void allAuthors() {
        BDDMockito.given(authorService.findAllAuthors())
                .willReturn(
                        List.of(
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

        var allAuthors =
                """
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
                .satisfies(
                        emails ->
                                assertThat(emails).contains("junit1@email.com", "junit2@email.com"))
                .path("allAuthors[*].firstName")
                .entityList(String.class)
                .satisfies(
                        names -> assertThat(names).containsAll(List.of("firstName", "secondName")));

        verify(authorService, times(1)).findAllAuthors();
        verifyNoMoreInteractions(authorService);
    }
}
