package com.learning.grafanalgtm;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.grafana.LgtmStackContainer;

@SpringBootTest(
        classes = {TestGrafanaLGTMApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GrafanaLGTMApplicationTest {

    @Autowired
    private LgtmStackContainer lgtmContainer;

    @LocalServerPort
    private int localServerPort;

    @BeforeAll
    void setUp() {
        PreemptiveBasicAuthScheme authScheme = new PreemptiveBasicAuthScheme();
        authScheme.setUserName("admin");
        authScheme.setPassword("admin");
        RestAssured.authentication = authScheme;
    }

    @BeforeEach
    void setPort() {
        RestAssured.port = localServerPort;
    }

    @Test
    void prometheus() {
        // calling endpoint to load metrics
        when().get("/greetings").then().statusCode(HttpStatus.SC_OK);

        RestAssured.port = lgtmContainer.getMappedPort(3000);
        given().contentType(ContentType.URLENC)
                .body("query=http_server_requests_seconds_count")
                .when()
                .post("/api/datasources/proxy/uid/prometheus/api/v1/query")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .body("status", is("success"))
                .body("data.resultType", is("vector"))
                .body("data.result", empty());
    }

    @Test
    void tempoQuery() {
        // calling endpoint to load metrics
        when().get("/greetings").then().statusCode(HttpStatus.SC_OK);

        RestAssured.port = lgtmContainer.getMappedPort(3000);

        given().contentType(ContentType.URLENC)
                .body("q={span.http.response.status_code=\"200\"}")
                .when()
                .post("/api/datasources/proxy/uid/tempo/api/search")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .body("metrics.completedJobs", is(1))
                .body("metrics.totalJobs", is(1))
                .log()
                .all();
    }
}
