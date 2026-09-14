package com.example.highrps.gatling.feeders;

import static io.gatling.javaapi.core.CoreDsl.*;

import com.example.highrps.gatling.config.LoadTestConfig;
import io.gatling.javaapi.core.FeederBuilder;
import java.nio.file.Paths;

public class MutationFeeder {

    public static FeederBuilder<String> mutablePosts() {
        return csv(Paths.get(LoadTestConfig.DATA_DIR + "/mutable_posts.csv")
                        .toAbsolutePath()
                        .toString())
                .random();
    }

    public static FeederBuilder<String> deletablePosts() {
        return csv(Paths.get(LoadTestConfig.DATA_DIR + "/deletable_posts.csv")
                        .toAbsolutePath()
                        .toString())
                .queue();
    }

    public static FeederBuilder<String> mutableComments() {
        return csv(Paths.get(LoadTestConfig.DATA_DIR + "/mutable_comments.csv")
                        .toAbsolutePath()
                        .toString())
                .random();
    }

    public static FeederBuilder<String> deletableComments() {
        return csv(Paths.get(LoadTestConfig.DATA_DIR + "/deletable_comments.csv")
                        .toAbsolutePath()
                        .toString())
                .queue();
    }
}
