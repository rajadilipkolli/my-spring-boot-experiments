package com.example.graphql.querydsl;

import com.example.graphql.querydsl.common.AbstractIntegrationTest;
import com.example.graphql.querydsl.model.PostCommentsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureHttpGraphQlTester
class GraphQueryDSLApplicationTests extends AbstractIntegrationTest {

  @Autowired private HttpGraphQlTester graphQlTester;

  @Test
  void contextLoads() {
    assertThat(POSTGRE_SQL_CONTAINER.isRunning()).isTrue();
  }

  @Test
  void test_query_insert() {
    String query =  """
             mutation {
                 createPost(postRequestDTO: {
                name: "unit",
                title: "title1",
                content: "content1",
                comments: [
                 {
                  review: "review1"
                 },
                 {
                  review: "review2"
                 }
                ],
                tags: [
                 {
                  name: "spring"
                 }
                ]
               }) {
                   title
                   content
                   createdBy
                   createdOn
                   comments {
                     review
                   }
                 }
               }
             """;
    this.graphQlTester
            .document(query)
            .execute()
            .path("createPost")
            .hasValue()
            .path("createPost.createdOn")
            .hasValue()
            .path("createPost.title")
            .entity(String.class).isEqualTo("title1")
            .path("createPost.comments")
            .hasValue().entityList(PostCommentsDTO.class)
            .contains(new PostCommentsDTO("review1"),new PostCommentsDTO("review2"));
  }
}
