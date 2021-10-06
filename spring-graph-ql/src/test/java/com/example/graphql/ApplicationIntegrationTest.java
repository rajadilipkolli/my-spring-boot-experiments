package com.example.graphql;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.record.CustomerDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.boot.test.tester.AutoConfigureGraphQlTester;
import org.springframework.graphql.test.tester.WebGraphQlTester;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureGraphQlTester
class ApplicationIntegrationTest extends AbstractIntegrationTest {

  @Autowired private WebGraphQlTester graphQlTester;

  @Test
  void contextLoads() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  @Test
  void test_query_all_customers() {
    this.graphQlTester
        .query("""
            {
              customers {
                id
                name
                orders {
                  id
                }
              }
            }
            """)
        .execute()
        .path("customers[*]")
        .pathExists()
        .valueIsNotEmpty()
        .entityList(CustomerDTO.class)
        .hasSize(4);
  }

  @Test
  void test_query_customers_by_name() {
    this.graphQlTester
        .query(
            """
                query ($name: String) {
                  customersByName(name: $name) {
                    id
                    name
                  }
                }
                """)
        .variable("$name", "raja")
        .execute()
        .errors().filter( error -> error.getPath().contains("customersByName"))
        .verify();
  }
}
