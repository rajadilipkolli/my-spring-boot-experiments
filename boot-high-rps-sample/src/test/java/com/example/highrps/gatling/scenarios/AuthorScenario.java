package com.example.highrps.gatling.scenarios;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.ChainBuilder;
import java.util.UUID;

public class AuthorScenario {

    /**
     * Builds the author registration and verification flow.
     *
     * @return the Gatling scenario chain
     */
    public static ChainBuilder register() {
        return exec(session -> {
                    String uuid = UUID.randomUUID().toString();
                    return session.set("newEmail", "new_author_" + uuid.substring(0, 8) + "@example.com")
                            .set("idempotencyKey", uuid);
                })
                .exec(http("author_register")
                        .post("/api/author")
                        .header("Idempotency-Key", "#{idempotencyKey}")
                        .body(
                                StringBody(
                                        "{\"firstName\":\"New\", \"lastName\":\"Author\", \"mobile\":\"1234567890\", \"email\":\"#{newEmail}\"}"))
                        .check(status().is(201))
                        .check(header("Location").saveAs("authorLocation")))
                // functional check: retrieve it
                .exec(http("Verify Author")
                        .get("#{authorLocation}")
                        .check(status().is(200))
                        .check(jsonPath("$.email").is(session -> session.getString("newEmail")))
                        .check(jsonPath("$.mobile").is("1234567890")));
    }
}
