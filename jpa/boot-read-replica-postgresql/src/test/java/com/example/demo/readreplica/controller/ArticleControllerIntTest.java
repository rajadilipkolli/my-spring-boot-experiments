package com.example.demo.readreplica.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.readreplica.domain.ArticleDTO;
import com.example.demo.readreplica.domain.CommentDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
class ArticleControllerIntTest {

    @Autowired private MockMvcTester mvcTester;

    @Autowired private ObjectMapper objectMapper;

    @Test
    void findArticleById() {

        mvcTester
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
                            assertThat(articleDTO.authored())
                                    .isNotNull()
                                    .isInstanceOf(LocalDateTime.class);
                            assertThat(articleDTO.published())
                                    .isNotNull()
                                    .isInstanceOf(LocalDateTime.class);
                            assertThat(articleDTO.commentDTOs())
                                    .isNotNull()
                                    .hasSize(2)
                                    .hasOnlyElementsOfType(CommentDTO.class);
                        });
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingArticle() {
        mvcTester.get().uri("/articles/99999").assertThat().hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void saveRetrieveAndDeleteArticle() throws JsonProcessingException {
        ArticleDTO articleDTO =
                new ArticleDTO(
                        "junitTitle",
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now(),
                        List.of(new CommentDTO("junitComment")));
        AtomicReference<String> location = new AtomicReference<>();
        mvcTester
                .post()
                .uri("/articles/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articleDTO))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .matches(
                        result -> {
                            location.set(result.getResponse().getHeader("Location"));
                            assertThat(location.get()).isNotBlank().contains("/articles/");
                        });

        mvcTester
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
                            assertThat(response.authored())
                                    .isNotNull()
                                    .isInstanceOf(LocalDateTime.class);
                            assertThat(response.authored().toLocalDate())
                                    .isEqualTo(LocalDate.now().minusDays(1));
                            assertThat(response.published())
                                    .isNotNull()
                                    .isInstanceOf(LocalDateTime.class);
                            assertThat(response.published().toLocalDate())
                                    .isEqualTo(LocalDate.now());
                            assertThat(response.commentDTOs())
                                    .isNotNull()
                                    .hasSize(1)
                                    .hasOnlyElementsOfType(CommentDTO.class);
                        });

        mvcTester.delete().uri(location.get()).assertThat().hasStatus(HttpStatus.ACCEPTED);
    }

    @Test
    void cantDeleteArticleWhenArticleNotFound() {
        mvcTester.delete().uri("/articles/99999").assertThat().hasStatus(HttpStatus.NOT_FOUND);
    }

}
