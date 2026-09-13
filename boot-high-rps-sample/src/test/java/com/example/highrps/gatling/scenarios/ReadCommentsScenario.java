package com.example.highrps.gatling.scenarios;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import com.example.highrps.gatling.feeders.PostFeeder;
import io.gatling.javaapi.core.ChainBuilder;

public class ReadCommentsScenario {

    public static ChainBuilder read() {
        return feed(PostFeeder.getUniform())
                .exec(http("Read Comments").get("/api/posts/#{postId}/comments").check(status().is(200)));
    }
}
