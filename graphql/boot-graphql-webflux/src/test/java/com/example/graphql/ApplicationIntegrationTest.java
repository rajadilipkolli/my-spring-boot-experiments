package com.example.graphql;

import static graphql.ErrorType.ValidationError;

import com.example.graphql.common.AbstractIntegrationTest;
import com.example.graphql.dtos.Customer;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.graphql.test.tester.GraphQlTester;

@AutoConfigureGraphQlTester
class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void query_all_customers() {
        this.graphQlTester
                .document("""
                        query {
                          customers(first:2) {
                            edges { node { id name } cursor }
                            pageInfo { hasNextPage }
                          }
                        }
                        """)
                .execute()
                .path("customers.edges[*].node.id")
                .entityList(Integer.class)
                .hasSize(2)
                .path("customers.pageInfo.hasNextPage")
                .entity(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    void query_customers_by_name() {
        this.graphQlTester
                .document("""
                        query ($name: String) {
                           customersByName(name: $name) {
                             id
                             name
                           }
                         }
                        """)
                .variable("name", "raja")
                .execute()
                .path("customersByName[*]")
                .hasValue()
                .entityList(Customer.class)
                .hasSize(1);
    }

    @Test
    void query_insert() {
        String randomString = RandomStringUtils.randomAlphabetic(5);
        String query = """
                mutation addCustomer($cname: String) {
                  addCustomer(name: $cname) {
                    id
                    name
                  }
                }
                """;
        this.graphQlTester
                .document(query)
                .variable("cname", randomString)
                .execute()
                .path("addCustomer")
                .hasValue()
                .path("addCustomer.id")
                .hasValue()
                .path("addCustomer.name")
                .entity(String.class)
                .isEqualTo(randomString);
    }

    @Test
    void query_insert_failure() {
        String query = """
                mutation addCustomer($cname: String) {
                  addCustomer(name: $cname) {
                    id
                    name
                  }
                }
                """;
        this.graphQlTester
                .document(query)
                .variable("cname", null)
                .execute()
                .errors()
                .expect(error -> error.getErrorType() == ValidationError)
                .verify()
                .path("$.data")
                .matchesJson("""
                        {
                            "addCustomer": null
                        }
                        """);
    }
}
