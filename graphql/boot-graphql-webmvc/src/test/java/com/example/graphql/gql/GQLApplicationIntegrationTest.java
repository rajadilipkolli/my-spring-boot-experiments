package com.example.graphql.gql;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.model.request.PostCommentRequest;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GQLApplicationIntegrationTest extends AbstractIntegrationTest {

    @Test
    @Order(1)
    void allAuthors() {
        graphQlTester
                .documentName("allAuthors")
                .execute()
                .path("allAuthors[*].email")
                .entityList(String.class)
                .satisfies(emails -> assertThat(emails).contains("user4@example.com"))
                .hasSize(4)
                .path("allAuthors[*].firstName")
                .entityList(String.class)
                .satisfies(names -> assertThat(names).contains("first name2"))
                .hasSize(4)
                .path("allAuthors[*].lastName")
                .entityList(String.class)
                .satisfies(names -> assertThat(names).contains("last name3"))
                .hasSize(4);
    }

    @Order(2)
    @ParameterizedTest
    @CsvSource({
        "user1@example.com, first name1, last name1, Title1",
        "user2@example.com, first name2, last name2, Title2",
        "user3@example.com, first name3, last name3, Title3",
        "user4@example.com, first name4, last name4, Title4"
    })
    void findAuthorByEmailId(String email, String firstName, String lastName, String title) {
        graphQlTester
                .documentName("findAuthorByEmail")
                .variable("emailId", email)
                .execute()
                .path("findAuthorByEmailId.email")
                .entity(String.class)
                .satisfies(emails -> assertThat(emails).contains(email))
                .path("findAuthorByEmailId.firstName")
                .entity(String.class)
                .satisfies(names -> assertThat(names).contains(firstName))
                .path("findAuthorByEmailId.lastName")
                .entity(String.class)
                .satisfies(names -> assertThat(names).contains(lastName))
                .path("findAuthorByEmailId.posts[*].title")
                .entityList(String.class)
                .satisfies(names -> assertThat(names).contains(title))
                .hasSize(2)
                .path("findAuthorByEmailId.posts[*].content")
                .entityList(String.class)
                .satisfies(names -> assertThat(names).contains("content1"))
                .hasSize(2)
                .path("findAuthorByEmailId.posts[*].comments[*].title")
                .entityList(String.class)
                .hasSize(3);
    }

    @Order(3)
    @Test
    void createAuthor() {
        Map<String, Object> inputValues = new HashMap<>();
        inputValues.put("firstName", "Junit");
        inputValues.put("lastName", "LastName");
        inputValues.put("email", "Junit@email.com");
        inputValues.put("mobile", "9876543210");

        graphQlTester
                .documentName("createAuthor")
                .variable("authorInput", inputValues)
                .execute()
                .path("createAuthor.email")
                .entity(String.class)
                .satisfies(emails -> assertThat(emails).contains("Junit@email.com"))
                .path("createAuthor.firstName")
                .entity(String.class)
                .satisfies(names -> assertThat(names).contains("Junit"))
                .path("createAuthor.lastName")
                .entity(String.class)
                .satisfies(names -> assertThat(names).contains("LastName"))
                .path("createAuthor.posts")
                .entityList(String.class)
                .hasSize(0);
    }

    @Test
    @Order(4)
    void createPost() {
        Map<String, Object> inputValues = new HashMap<>();
        inputValues.put("title", "JunitTitle");
        inputValues.put("content", "JunitContent");
        inputValues.put("email", "Junit@email.com");
        inputValues.put("published", true);
        Map<String, String> detailsValues = new HashMap<>();
        detailsValues.put("detailsKey", "newPost");
        inputValues.put("details", detailsValues);

        graphQlTester
                .documentName("createPost")
                .variable("newPostRequest", inputValues)
                .execute()
                .path("createPost.id")
                .entity(Long.class)
                .satisfies(id -> assertThat(id).isPositive())
                .path("createPost.published")
                .entity(Boolean.class)
                .satisfies(published -> assertThat(published).isTrue())
                .path("createPost.publishedAt")
                .entity(LocalDateTime.class)
                .satisfies(localDateTime -> assertThat(localDateTime).isNotNull())
                .path("createPost.title")
                .entity(String.class)
                .isEqualTo("JunitTitle")
                .path("createPost.content")
                .entity(String.class)
                .isEqualTo("JunitContent")
                .path("createPost.details.detailsKey")
                .entity(String.class)
                .isEqualTo("newPost");
    }

    @Test
    @Order(5)
    // adding ordering to make sure others are not failed
    void addCommentToPost() {
        PostCommentRequest postCommentRequest = new PostCommentRequest("JunitTitle", "JunitContent", "1", true);

        graphQlTester
                .documentName("addCommentToPost")
                .variable("addCommentToPostRequest", postCommentRequest)
                .execute()
                .path("addCommentToPost.commentId")
                .entity(Long.class)
                .satisfies(id -> assertThat(id).isPositive())
                .path("addCommentToPost.published")
                .entity(Boolean.class)
                .satisfies(published -> assertThat(published).isTrue())
                .path("addCommentToPost.publishedAt")
                .entity(OffsetDateTime.class)
                .satisfies(localDateTime -> assertThat(localDateTime).isNotNull())
                .path("addCommentToPost.title")
                .entity(String.class)
                .isEqualTo("JunitTitle")
                .path("addCommentToPost.content")
                .entity(String.class)
                .isEqualTo("JunitContent")
                .path("addCommentToPost.createdAt")
                .entity(LocalDateTime.class)
                .satisfies(localDateTime -> assertThat(localDateTime).isNotNull());
    }
}
