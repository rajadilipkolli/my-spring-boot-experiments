package com.learning.grafanalgtm;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.learning.grafanalgtm.common.ContainerConfig;
import io.restassured.RestAssured;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.grafana.LgtmStackContainer;

@SpringBootTest(
        classes = {ContainerConfig.class},
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
        when().get("/greetings?username=boot")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .header("X-Trace-Id", not(empty()))
                .body("message", is("Hello, boot!"));

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
                .body("data.result", empty())
                .log()
                .all();
    }

    @Test
    void queryPrometheus() {
        when().get("/greetings")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .header("X-Trace-Id", not(empty()))
                .body("message", is("Hello, Guest!"));

        RestAssured.port = lgtmContainer.getMappedPort(9090);
        given().contentType(ContentType.URLENC)
                .body("query=up")
                .when()
                .post("/api/v1/query")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .body("status", is("success"))
                .body("data.resultType", is("vector"))
                .body("data.result", not(empty()))
                .body("data.result[0].value[1]", is("1")) // Verify service is up
                .log()
                .all();
    }

    @Test
    void tempoQuery() {
        when().get("/greetings")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(ContentType.JSON)
                .header("X-Trace-Id", not(empty()))
                .body("message", is("Hello, Guest!"));

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
