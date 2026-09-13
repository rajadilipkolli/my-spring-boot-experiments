package com.example.highrps.gatling.feeders;

import static io.gatling.javaapi.core.CoreDsl.*;

import com.example.highrps.gatling.config.LoadTestConfig;
import io.gatling.javaapi.core.FeederBuilder;
import java.nio.file.Paths;

public class AuthorFeeder {

    public static FeederBuilder<String> get() {
        return csv(Paths.get(LoadTestConfig.DATA_DIR + "/authors.csv")
                        .toAbsolutePath()
                        .toString())
                .random();
    }
}
