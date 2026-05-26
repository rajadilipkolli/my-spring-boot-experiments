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
        var r1 = new AuthorResponse(
                1L, "firstName", "middleName", "lastName", 9848022338L, "junit1@email.com", LocalDateTime.now());
        var r2 = new AuthorResponse(
                2L, "secondName", "middleName", "lastName", 9848022338L, "junit2@email.com", LocalDateTime.now());

        // simple Window implementation for test
        var window = new org.springframework.data.domain.Window<AuthorResponse>() {
            private final java.util.List<AuthorResponse> content = List.of(r1, r2);

            @Override
            public int size() {
                return content.size();
            }

            @Override
            public boolean isEmpty() {
                return content.isEmpty();
            }

            @Override
            public java.util.List<AuthorResponse> getContent() {
                return content;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public org.springframework.data.domain.ScrollPosition positionAt(int index) {
                return org.springframework.data.domain.ScrollPosition.offset(0);
            }

            @Override
            public <U> org.springframework.data.domain.Window<U> map(
                    java.util.function.Function<? super AuthorResponse, ? extends U> converter) {
                return null;
            }

            @Override
            public java.util.Iterator<AuthorResponse> iterator() {
                return content.iterator();
            }
        };

        given(authorService.findAllAuthors(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt()))
                .willReturn(window);

        var allAuthors = """
                query authorEntities{
                    allAuthors(first:2){
                      edges { node { id firstName email } cursor }
                      pageInfo { hasNextPage startCursor endCursor }
                    }
                 }""";

        graphQlTester
                .document(allAuthors)
                .execute()
                .path("allAuthors.edges[*].node.email")
                .entityList(String.class)
                .satisfies(emails -> assertThat(emails).contains("junit1@email.com", "junit2@email.com"))
                .path("allAuthors.pageInfo.hasNextPage")
                .entity(Boolean.class)
                .satisfies(hasNext -> assertThat(hasNext).isFalse());

        verify(authorService, times(1))
                .findAllAuthors(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt());
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
