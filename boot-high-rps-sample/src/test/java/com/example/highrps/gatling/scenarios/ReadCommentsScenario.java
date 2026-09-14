package com.example.highrps.gatling.scenarios;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import com.example.highrps.gatling.feeders.PostFeeder;
import io.gatling.javaapi.core.ChainBuilder;

public class ReadCommentsScenario {

    /**
     * Builds the flow that reads comments for a random post.
     *
     * @return the Gatling scenario chain
     */
    public static ChainBuilder read() {
        return feed(PostFeeder.getUniform())
                .exec(http("comment_read").get("/api/posts/#{postId}/comments").check(status().is(200)));
    }
}
