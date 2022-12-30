package com.example.graphql;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.graphql.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

@AutoConfigureHttpGraphQlTester
class ApplicationIntegrationTest extends AbstractIntegrationTest {

    @Autowired private HttpGraphQlTester graphQlTester;

    @Test
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
                .satisfies(names -> assertThat(names).contains("firstName2"))
                .hasSize(4)
                .path("allAuthors[*].lastName")
                .entityList(String.class)
                .satisfies(names -> assertThat(names).contains("lastName3"))
                .hasSize(4);
    }
}
