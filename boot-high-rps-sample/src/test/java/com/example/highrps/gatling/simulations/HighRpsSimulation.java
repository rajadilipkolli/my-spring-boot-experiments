package com.example.highrps.gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import com.example.highrps.gatling.config.LoadTestConfig;
import com.example.highrps.gatling.scenarios.*;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * Configures the selected load profile and its regression assertions.
     */
    public HighRpsSimulation() {
        double targetRps = LoadTestConfig.TARGET_RPS;
        int durationMins = LoadTestConfig.DURATION_MINUTES;
        int warmupMins = LoadTestConfig.WARMUP_MINUTES;

        boolean stressProfile = "stress".equalsIgnoreCase(LoadTestConfig.PROFILE);
        PopulationBuilder population =
                scn.injectOpen(injectionSteps(stressProfile, targetRps, durationMins, warmupMins));

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

    static List<OpenInjectionStep> injectionSteps(
            boolean stressProfile, double targetRps, int durationMins, int warmupMins) {
        List<OpenInjectionStep> steps = new ArrayList<>();
        steps.add(nothingFor(5));

        if (warmupMins != 0) {
            double warmupTargetRps = stressProfile ? 100 : targetRps;
            steps.add(rampUsersPerSec(0).to(warmupTargetRps).during(Duration.ofMinutes(warmupMins)));
        }

        if (stressProfile) {
            steps.add(constantUsersPerSec(100).during(Duration.ofMinutes(durationMins)));
            steps.add(rampUsersPerSec(100).to(250).during(Duration.ofMinutes(5)));
            steps.add(constantUsersPerSec(250).during(Duration.ofMinutes(durationMins)));
            steps.add(rampUsersPerSec(250).to(500).during(Duration.ofMinutes(5)));
            steps.add(constantUsersPerSec(500).during(Duration.ofMinutes(durationMins)));
            steps.add(rampUsersPerSec(500).to(1000).during(Duration.ofMinutes(5)));
            steps.add(constantUsersPerSec(1000).during(Duration.ofMinutes(durationMins)));
        } else {
            steps.add(constantUsersPerSec(targetRps).during(Duration.ofMinutes(durationMins)));
        }

        return steps;
    }
}
