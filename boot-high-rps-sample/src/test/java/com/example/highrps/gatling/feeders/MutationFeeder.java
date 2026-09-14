package com.example.highrps.gatling.feeders;

import static io.gatling.javaapi.core.CoreDsl.*;

import com.example.highrps.gatling.config.LoadTestConfig;
import io.gatling.javaapi.core.FeederBuilder;
import java.nio.file.Paths;

public class MutationFeeder {

    /**
     * Creates a random feeder for posts that may be updated repeatedly.
     *
     * @return the mutable post feeder
     */
    public static FeederBuilder<String> mutablePosts() {
        return csv(Paths.get(LoadTestConfig.DATA_DIR + "/mutable_posts.csv")
                        .toAbsolutePath()
                        .toString())
                .random();
    }

    /**
     * Creates a queue feeder that consumes each deletable post once.
     *
     * @return the deletable post feeder
     */
    public static FeederBuilder<String> deletablePosts() {
        return csv(Paths.get(LoadTestConfig.DATA_DIR + "/deletable_posts.csv")
                        .toAbsolutePath()
                        .toString())
                .queue();
    }

    /**
     * Creates a random feeder for comments that may be updated repeatedly.
     *
     * @return the mutable comment feeder
     */
    public static FeederBuilder<String> mutableComments() {
        return csv(Paths.get(LoadTestConfig.DATA_DIR + "/mutable_comments.csv")
                        .toAbsolutePath()
                        .toString())
                .random();
    }

    /**
     * Creates a queue feeder that consumes each deletable comment once.
     *
     * @return the deletable comment feeder
     */
    public static FeederBuilder<String> deletableComments() {
        return csv(Paths.get(LoadTestConfig.DATA_DIR + "/deletable_comments.csv")
                        .toAbsolutePath()
                        .toString())
                .queue();
    }
}
