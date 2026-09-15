package com.example.highrps.gatling.scenarios;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import com.example.highrps.gatling.feeders.MutationFeeder;
import com.example.highrps.gatling.feeders.PostFeeder;
import io.gatling.javaapi.core.ChainBuilder;
import java.util.UUID;

public class CommentScenario {

    /**
     * Builds the comment creation flow.
     *
     * @return the Gatling scenario chain
     */
    public static ChainBuilder create() {
        return feed(PostFeeder.getUniform())
                .exec(session -> {
                    String uuid = UUID.randomUUID().toString();
                    return session.set("idempotencyKey", uuid)
                            .set("randomTitle", "Gatling Comment " + uuid.substring(0, 8));
                })
                .exec(http("comment_create")
                        .post("/api/posts/#{postId}/comments")
                        .header("Idempotency-Key", "#{idempotencyKey}")
                        .body(
                                StringBody(
                                        "{\"title\":\"#{randomTitle}\", \"content\":\"Great post!\", \"postId\":#{postId}, \"published\":true}"))
                        .check(status().is(201)));
    }

    /**
     * Builds the comment update and read-back verification flow.
     *
     * @return the Gatling scenario chain
     */
    public static ChainBuilder update() {
        return feed(MutationFeeder.mutableComments())
                .exec(session -> {
                    String uuid = UUID.randomUUID().toString();
                    return session.set("idempotencyKey", uuid)
                            .set("randomTitle", "Updated Comment " + uuid.substring(0, 8))
                            .set("randomContent", "Updated Content " + uuid.substring(0, 8));
                })
                .exec(http("comment_update")
                        .put("/api/posts/#{postId}/comments/#{commentId}")
                        .header("Idempotency-Key", "#{idempotencyKey}")
                        .body(
                                StringBody(
                                        "{\"title\":\"#{randomTitle}\", \"content\":\"#{randomContent}\", \"postId\":#{postId}, \"published\":true}"))
                        .check(status().is(200)))
                .exec(http("Verify Updated Comment")
                        .get("/api/posts/#{postId}/comments/#{commentId}")
                        .check(status().is(200))
                        .check(jsonPath("$.postId").is(session -> session.getString("postId"))));
    }

    /**
     * Builds the comment deletion and not-found verification flow.
     *
     * @return the Gatling scenario chain
     */
    public static ChainBuilder delete() {
        return feed(MutationFeeder.deletableComments())
                .exec(session -> session.set("idempotencyKey", UUID.randomUUID().toString()))
                .exec(http("comment_delete")
                        .delete("/api/posts/#{postId}/comments/#{commentId}")
                        .header("Idempotency-Key", "#{idempotencyKey}")
                        .check(status().is(204)))
                .exec(http("Verify Deleted Comment")
                        .get("/api/posts/#{postId}/comments/#{commentId}")
                        .check(status().is(404)));
    }
}
