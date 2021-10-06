package com.example.graphql;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.dtos.Customer;
import com.example.graphql.dtos.CustomerDTO;
import org.apache.commons.lang3.RandomStringUtils;
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
        .hasSizeGreaterThan(4);
  }

  @Test
  void test_query_customers_by_name() {
    this.graphQlTester
        .query(
            """
                query {
                  customersByName(name: "raja") {
                    id
                    name
                  }
                }
                """)
        .execute()
        .path("customersByName[*]")
        .pathExists()
        .valueIsNotEmpty()
        .entityList(Customer.class)
        .hasSize(1);
  }

    @Test
    void test_query_insert() {
      String randomString = RandomStringUtils.randomAlphabetic(5);
      String query =  """
                    mutation {
                      addCustomer(name: "$cname") {
                        id
                        name
                      }
                    }
                    """;
        String finalQuery = query.replace("$cname", randomString);
        this.graphQlTester
            .query(finalQuery)
            .execute()
            .path("addCustomer")
            .pathExists()
            .valueIsNotEmpty()
            .path("addCustomer.id")
            .pathExists()
            .path("addCustomer.name")
            .entity(String.class).isEqualTo(randomString);
    }
}
