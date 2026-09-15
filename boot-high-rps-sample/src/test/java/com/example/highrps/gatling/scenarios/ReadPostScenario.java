package com.example.highrps.gatling.scenarios;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import com.example.highrps.gatling.feeders.PostFeeder;
import io.gatling.javaapi.core.ChainBuilder;
import java.util.Map;

public class ReadPostScenario {

    /**
     * Builds the weighted post-read flow with optional comment retrieval.
     *
     * @return the Gatling scenario chain
     */
    public static ChainBuilder read() {
        return exec(session -> {
                    Map<String, Object> post = PostFeeder.getSkewed().next();
                    return session.set("postId", post.get("postId"));
                })
                .exec(http("post_read")
                        .get("/api/posts/#{postId}")
                        .check(status().is(200))
                        .check(jsonPath("$.authorEmail").exists())
                        .check(jsonPath("$.tags").exists()))
                // optionally chain comments get
                .randomSwitch()
                .on(percent(50.0)
                        .then(exec(http("Read Post Comments")
                                .get("/api/posts/#{postId}/comments")
                                .check(status().is(200)))));
    }
}
