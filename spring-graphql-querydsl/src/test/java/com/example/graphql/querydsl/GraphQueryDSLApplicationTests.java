package com.example.graphql.querydsl;

import com.example.graphql.querydsl.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.boot.test.tester.AutoConfigureGraphQlTester;
import org.springframework.graphql.test.tester.WebGraphQlTester;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureGraphQlTester
class GraphQueryDSLApplicationTests extends AbstractIntegrationTest {

  @Autowired private WebGraphQlTester graphQlTester;

  @Test
  void contextLoads() {
    assertThat(POSTGRE_SQL_CONTAINER.isRunning()).isTrue();
  }

    @Test
    void test_query_insert() {
        String query =  """
                    mutation {
                        createPost(postRequestDTO: {
                      	name: "junit",
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
                .entity(String.class).isEqualTo("title2")
                .path("createPost.comments")
                .valueExists().entityList(String.class).contains("review1","review2");
    }
}
