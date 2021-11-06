package com.example.graphql.querydsl;

import com.example.graphql.querydsl.common.AbstractIntegrationTest;
import com.example.graphql.querydsl.model.PostCommentsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.boot.test.tester.AutoConfigureGraphQlTester;
import org.springframework.graphql.test.tester.GraphQlTester;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebGraphQlTester
class GraphQueryDSLApplicationTests extends AbstractIntegrationTest {

  @Autowired private GraphQlTester graphQlTester;

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
            .query(query)
            .execute()
            .path("createPost")
            .pathExists()
            .valueIsNotEmpty()
            .path("createPost.createdOn")
            .pathExists()
            .path("createPost.title")
            .entity(String.class).isEqualTo("title1")
            .path("createPost.comments")
            .valueExists().entityList(PostCommentsDTO.class)
            .contains(new PostCommentsDTO("review1"),new PostCommentsDTO("review2"));
  }
}
