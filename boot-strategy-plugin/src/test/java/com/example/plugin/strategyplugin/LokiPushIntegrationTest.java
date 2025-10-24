package com.example.plugin.strategyplugin;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.plugin.strategyplugin.common.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;

/**
 * Integration test: call the application's /fetch endpoint (which produces logs) and then query
 * Loki for a unique token to confirm the integration end-to-end without pushing manually.
 */
class LokiPushIntegrationTest extends AbstractIntegrationTest {

    @Value("${loki.uri}")
    private String lokiPushUri; // expected to be something like http://host:3100/loki/api/v1/push

    @LocalServerPort private int port;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void fetchEndpointProducesLogsThatAppearInLoki() {
        String unique = "loki-test-" + UUID.randomUUID();

        // Call the application's /fetch endpoint with a unique type value to generate identifiable
        // logs.
        // We intentionally ignore the HTTP status (could be 200 or 400) because the controller logs
        // the incoming request in both cases and that log entry is what we verify in Loki.
        for (int i = 0; i < 5; i++) {
            this.mockMvcTester
                    .get()
                    .uri("/fetch")
                    .param("type", unique)
                    .accept(MediaType.APPLICATION_JSON)
                    .assertThat()
                    .hasContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        }

        // Build base URL for Loki queries from configured push URI
        String base;
        if (lokiPushUri.contains("/loki/api/v1/push")) {
            base = lokiPushUri.substring(0, lokiPushUri.indexOf("/loki/api/v1/push"));
        } else {
            int idx = lokiPushUri.indexOf("/loki");
            base = idx > 0 ? lokiPushUri.substring(0, idx) : lokiPushUri;
        }

        long startNs = (Instant.now().minusSeconds(60).toEpochMilli()) * 1_000_000L;
        long endNs = (Instant.now().plusSeconds(60).toEpochMilli()) * 1_000_000L;

        // Try a few valid LogQL queries (app label, agent label, catch-all) that search for the
        // unique token
        String[] logqlCandidates =
                new String[] {
                    String.format("{app=\"strategy-plugin-service\"} |= \"%s\"", unique),
                    String.format("{agent=\"loki4j\"} |= \"%s\"", unique),
                    String.format("{} |= \"%s\"", unique)
                };

        await().atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(
                        () -> {
                            boolean found = false;
                            for (String logql : logqlCandidates) {
                                String encodedQuery =
                                        URLEncoder.encode(logql, StandardCharsets.UTF_8);
                                String qUrl =
                                        base
                                                + "/loki/api/v1/query_range?query="
                                                + encodedQuery
                                                + "&start="
                                                + startNs
                                                + "&end="
                                                + endNs
                                                + "&limit=200";

                                HttpRequest q =
                                        HttpRequest.newBuilder()
                                                .uri(URI.create(qUrl))
                                                .timeout(Duration.ofSeconds(10))
                                                .GET()
                                                .build();
                                HttpResponse<String> resp =
                                        http.send(q, HttpResponse.BodyHandlers.ofString());

                                if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                                    // try next candidate
                                    continue;
                                }

                                JsonNode root = objectMapper.readTree(resp.body());
                                JsonNode data = root.path("data").path("result");
                                if (data.isArray()) {
                                    for (JsonNode item : data) {
                                        JsonNode values = item.path("values");
                                        if (values.isArray()) {
                                            for (JsonNode v : values) {
                                                if (v.isArray() && v.size() > 1) {
                                                    String line = v.get(1).asText();
                                                    if (line.contains(unique)) {
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if (found) break;
                                    }
                                }

                                if (found) break;
                            }

                            assertTrue(found, "expected message not found yet in Loki");
                        });
    }
}
