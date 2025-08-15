package com.example.choasmonkey.gatling;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.http.HttpDsl.header;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CustomerRequestSimulation extends Simulation {

    final HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .userAgentHeader("Gatling/Performance Test");

    final Iterator<Map<String, Object>> feeder = Stream.generate((Supplier<Map<String, Object>>)
                    () -> Collections.singletonMap("text", UUID.randomUUID().toString()))
            .iterator();

    final ScenarioBuilder scn = CoreDsl.scenario("Load Test Creating Customers")
            .feed(feeder)
            .exec(http("create-customer-request")
                    .post("/api/customers")
                    .header("Content-Type", "application/json")
                    .body(StringBody("{ \"text\": \"#{text}\" }"))
                    .check(status().is(201))
                    .check(header("Location").saveAs("location")))
            .exec(http("get-customer-request")
                    .get(session -> session.getString("location"))
                    .check(status().is(200)));

    public CustomerRequestSimulation() {
        this.setUp(scn.injectOpen(constantUsersPerSec(100).during(Duration.ofSeconds(30))))
                .protocols(httpProtocol)
                .assertions(global().failedRequests().count().is(0L));
    }
}
