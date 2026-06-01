package com.example.graphql;

import static graphql.ErrorType.ValidationError;
import static org.assertj.core.api.Assertions.assertThat;

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
    void query_customers_with_after_cursor() {
        var firstPage = this.graphQlTester.document("""
                        query {
                          customers(first:2) {
                            edges { node { id name } cursor }
                            pageInfo { hasNextPage hasPreviousPage }
                          }
                        }
                        """).execute();

        String afterCursor =
                firstPage.path("customers.edges[1].cursor").entity(String.class).get();

        this.graphQlTester
                .document("""
                        query($after: String) {
                          customers(first:2, after:$after) {
                            edges { node { id name } cursor }
                            pageInfo { hasNextPage hasPreviousPage }
                          }
                        }
                        """)
                .variable("after", afterCursor)
                .execute()
                .path("customers.edges[*].node.id")
                .entityList(Integer.class)
                .satisfies(ids -> assertThat(ids).containsExactly(3, 4))
                .path("customers.pageInfo.hasPreviousPage")
                .entity(Boolean.class)
                .isEqualTo(true)
                .path("customers.pageInfo.hasNextPage")
                .entity(Boolean.class)
                .isEqualTo(false);
    }

    @Test
    void query_customers_with_invalid_after_cursor() {
        this.graphQlTester.document("""
                        query {
                          customers(first:2, after:"invalid-cursor") {
                            edges { node { id name } cursor }
                            pageInfo { hasNextPage hasPreviousPage }
                          }
                        }
                        """).execute().errors().satisfy(errors -> assertThat(errors)
                .hasSize(1));
    }

    @Test
    void query_customers_with_last_before_cursor() {
        var firstPage = this.graphQlTester.document("""
                        query {
                          customers(first:2) {
                            edges { node { id name } cursor }
                            pageInfo { hasNextPage hasPreviousPage endCursor }
                          }
                        }
                        """).execute();

        String afterCursor =
                firstPage.path("customers.edges[1].cursor").entity(String.class).get();

        var secondPage =
                this.graphQlTester.document("""
                        query($after: String) {
                          customers(first:2, after:$after) {
                            edges { node { id name } cursor }
                            pageInfo { hasNextPage hasPreviousPage startCursor endCursor }
                          }
                        }
                        """).variable("after", afterCursor).execute();

        String beforeCursor = secondPage
                .path("customers.edges[0].cursor")
                .entity(String.class)
                .get();

        this.graphQlTester
                .document("""
                        query($before: String) {
                          customers(last:2, before:$before) {
                            edges { node { id name } cursor }
                            pageInfo { hasNextPage hasPreviousPage }
                          }
                        }
                        """)
                .variable("before", beforeCursor)
                .execute()
                .path("customers.edges[*].node.id")
                .entityList(Integer.class)
                .satisfies(ids -> assertThat(ids).containsExactly(1, 2))
                .path("customers.pageInfo.hasPreviousPage")
                .entity(Boolean.class)
                .isEqualTo(false)
                .path("customers.pageInfo.hasNextPage")
                .entity(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    void query_customers_with_last_only() {
        this.graphQlTester
                .document("""
                        query {
                          customers(last:2) {
                            edges { node { id name } cursor }
                            pageInfo { hasNextPage hasPreviousPage }
                          }
                        }
                        """)
                .execute()
                .path("customers.edges[*].node.id")
                .entityList(Integer.class)
                .satisfies(ids -> assertThat(ids).containsExactly(3, 4))
                .path("customers.pageInfo.hasPreviousPage")
                .entity(Boolean.class)
                .isEqualTo(true)
                .path("customers.pageInfo.hasNextPage")
                .entity(Boolean.class)
                .isEqualTo(false);
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
