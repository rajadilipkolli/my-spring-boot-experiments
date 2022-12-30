package com.example.graphql.gql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.graphql.entities.AuthorEntity;
import com.example.graphql.services.AuthorService;
import com.example.graphql.services.PostCommentService;
import com.example.graphql.services.PostService;
import com.example.graphql.services.TagService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

@GraphQlTest(AuthorGraphQlController.class)
class AuthorQueryTest {

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
                                AuthorEntity.builder()
                                        .id(1L)
                                        .firstName("test title")
                                        .email("junit1@email.com")
                                        .build(),
                                AuthorEntity.builder()
                                        .id(2L)
                                        .firstName("test title2")
                                        .email("junit2@email.com")
                                        .build()));

        var allAuthors =
                """
                query authors{
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
                        names ->
                                assertThat(names)
                                        .containsAll(List.of("test title", "test title2")));

        verify(authorService, times(1)).findAllAuthors();
        verifyNoMoreInteractions(authorService);
    }
}
