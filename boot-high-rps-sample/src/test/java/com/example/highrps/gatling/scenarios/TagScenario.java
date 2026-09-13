package com.example.highrps.gatling.scenarios;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import com.example.highrps.gatling.feeders.TagFeeder;
import io.gatling.javaapi.core.ChainBuilder;
import java.util.List;

public class TagScenario {

    public static ChainBuilder read() {
        return feed(TagFeeder.getTags())
                .exec(session -> {
                    String tag = session.getString("tag");
                    List<String> posts = TagFeeder.getPostsForTag(tag);
                    // limit to 5 to avoid enormous client-side fan-out in test
                    int limit = Math.min(posts.size(), 5);
                    return session.set("postIdsToFetch", posts.subList(0, limit));
                })
                /*
                 * The API has no native tag query endpoint (per Assumption 2).
                 * We resolve the tag to its associated post IDs client-side via the feeder
                 * and fetch each post individually.
                 */
                .foreach("#{postIdsToFetch}", "postId")
                .on(exec(http("Read Post By Tag").get("/api/posts/#{postId}").check(status().is(200))));
    }
}
