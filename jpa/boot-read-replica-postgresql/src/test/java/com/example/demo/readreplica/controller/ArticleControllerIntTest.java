package com.example.demo.readreplica.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.readreplica.common.AbstractIntegrationTest;
import com.example.demo.readreplica.domain.ArticleDTO;
import com.example.demo.readreplica.domain.CommentDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class ArticleControllerIntTest extends AbstractIntegrationTest {

    @Test
    void findArticleById() {

        mockMvcTester
                .get()
                .uri("/articles/1")
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(ArticleDTO.class)
                .satisfies(
                        articleDTO -> {
                            assertThat(articleDTO.title())
                                    .isNotNull()
                                    .isEqualTo("Waiter! There is a bug in my JSoup!");
                            assertThat(articleDTO.authored()).isInstanceOf(LocalDateTime.class);
                            assertThat(articleDTO.published()).isInstanceOf(LocalDateTime.class);
                            assertThat(articleDTO.commentDTOs())
                                    .hasSize(2)
                                    .hasOnlyElementsOfType(CommentDTO.class);
                        });
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingArticle() {
        mockMvcTester.get().uri("/articles/99999").assertThat().hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void saveRetrieveAndDeleteArticle() {
        ArticleDTO articleDTO =
                new ArticleDTO(
                        "junitTitle",
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now(),
                        List.of(new CommentDTO("junitComment")));
        AtomicReference<String> location = new AtomicReference<>();
        mockMvcTester
                .post()
                .uri("/articles/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(articleDTO))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .matches(
                        result -> {
                            location.set(result.getResponse().getHeader("Location"));
                            assertThat(location.get()).isNotBlank().contains("/articles/");
                        });

        mockMvcTester
                .get()
                .uri(location.get())
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(ArticleDTO.class)
                .satisfies(
                        response -> {
                            assertThat(response.title()).isNotNull().isEqualTo("junitTitle");
                            assertThat(response.authored()).isInstanceOf(LocalDateTime.class);
                            assertThat(response.authored().toLocalDate())
                                    .isEqualTo(LocalDate.now().minusDays(1));
                            assertThat(response.published()).isInstanceOf(LocalDateTime.class);
                            assertThat(response.published().toLocalDate())
                                    .isEqualTo(LocalDate.now());
                            assertThat(response.commentDTOs())
                                    .hasSize(1)
                                    .hasOnlyElementsOfType(CommentDTO.class);
                        });

        mockMvcTester.delete().uri(location.get()).assertThat().hasStatus(HttpStatus.ACCEPTED);
    }

    @Test
    void cantDeleteArticleWhenArticleNotFound() {
        mockMvcTester.delete().uri("/articles/99999").assertThat().hasStatus(HttpStatus.NOT_FOUND);
    }
}
