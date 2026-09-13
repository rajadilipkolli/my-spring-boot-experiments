package com.example.highrps.gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import com.example.highrps.gatling.config.LoadTestConfig;
import com.example.highrps.gatling.scenarios.*;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;

public class HighRpsSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http.baseUrl(LoadTestConfig.BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("High RPS Scenario")
            .randomSwitch()
            .on(
                    percent(LoadTestConfig.READ_POST_WEIGHT).then(ReadPostScenario.read()),
                    percent(LoadTestConfig.READ_COMMENTS_WEIGHT).then(ReadCommentsScenario.read()),
                    percent(LoadTestConfig.READ_TAG_POSTS_WEIGHT).then(TagScenario.read()),
                    percent(LoadTestConfig.CREATE_COMMENT_WEIGHT).then(CommentScenario.create()),
                    percent(LoadTestConfig.CREATE_POST_WEIGHT).then(PostScenario.create()),
                    percent(LoadTestConfig.REGISTER_AUTHOR_WEIGHT).then(AuthorScenario.register()));

    public HighRpsSimulation() {
        double targetRps = LoadTestConfig.TARGET_RPS;
        int durationMins = LoadTestConfig.DURATION_MINUTES;
        int warmupMins = LoadTestConfig.WARMUP_MINUTES;

        PopulationBuilder population;

        if ("stress".equalsIgnoreCase(LoadTestConfig.PROFILE)) {
            population = scn.injectOpen(
                    nothingFor(5),
                    rampUsersPerSec(0).to(100).during(Duration.ofMinutes(warmupMins)),
                    constantUsersPerSec(100).during(Duration.ofMinutes(durationMins)),
                    rampUsersPerSec(100).to(250).during(Duration.ofMinutes(5)),
                    constantUsersPerSec(250).during(Duration.ofMinutes(durationMins)),
                    rampUsersPerSec(250).to(500).during(Duration.ofMinutes(5)),
                    constantUsersPerSec(500).during(Duration.ofMinutes(durationMins)),
                    rampUsersPerSec(500).to(1000).during(Duration.ofMinutes(5)),
                    constantUsersPerSec(1000).during(Duration.ofMinutes(durationMins)));
        } else {
            population = scn.injectOpen(
                    nothingFor(5),
                    rampUsersPerSec(0).to(targetRps).during(Duration.ofMinutes(warmupMins)),
                    constantUsersPerSec(targetRps).during(Duration.ofMinutes(durationMins)));
        }

        setUp(population)
                .protocols(httpProtocol)
                .assertions(
                        global().failedRequests().percent().lte(LoadTestConfig.MAX_ERROR_RATE),
                        global().responseTime().percentile3().lte((int) (LoadTestConfig.BASELINE_P95_MS
                                * (1.0 + LoadTestConfig.ALLOWED_P95_DELTA_PERCENT / 100.0))),
                        global().responseTime().percentile4().lte((int) (LoadTestConfig.BASELINE_P99_MS
                                * (1.0 + LoadTestConfig.ALLOWED_P99_DELTA_PERCENT / 100.0))),
                        global().requestsPerSec()
                                .gte(LoadTestConfig.BASELINE_THROUGHPUT
                                        * (1.0 + LoadTestConfig.ALLOWED_THROUGHPUT_DELTA_PERCENT / 100.0)));
    }
}
