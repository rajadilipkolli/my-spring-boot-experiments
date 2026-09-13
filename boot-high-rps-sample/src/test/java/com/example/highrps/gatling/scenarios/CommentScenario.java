package com.example.highrps.gatling.scenarios;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import com.example.highrps.gatling.feeders.PostFeeder;
import io.gatling.javaapi.core.ChainBuilder;
import java.util.UUID;

public class CommentScenario {

    public static ChainBuilder create() {
        return feed(PostFeeder.getUniform())
                .exec(session -> {
                    String uuid = UUID.randomUUID().toString();
                    return session.set("idempotencyKey", uuid)
                            .set("randomTitle", "Gatling Comment " + uuid.substring(0, 8));
                })
                .exec(http("Create Comment")
                        .post("/api/post-comments")
                        .header("Idempotency-Key", "#{idempotencyKey}")
                        .body(
                                StringBody(
                                        "{\"title\":\"#{randomTitle}\", \"content\":\"Great post!\", \"postId\":#{postId}, \"published\":true}"))
                        .check(status().is(201)));
    }
}
