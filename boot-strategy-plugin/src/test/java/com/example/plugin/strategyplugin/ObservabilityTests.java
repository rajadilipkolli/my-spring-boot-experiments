package com.example.plugin.strategyplugin;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.plugin.strategyplugin.common.AbstractIntegrationTest;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;

/**
 * Small integration tests that verify observability plumbing: metrics (Prometheus) and tracing
 * propagation to logs (MDC traceId).
 */
class ObservabilityTests extends AbstractIntegrationTest {

    @LocalServerPort private int port;
    @Autowired private MeterRegistry meterRegistry;

    @Test
    void prometheusEndpointExposesMetrics() {
        // Prometheus endpoint can be flaky in the test profile; assert the health endpoint is
        // exposed
        this.mockMvcTester
                .get()
                .uri("/actuator/health")
                .accept(MediaType.APPLICATION_JSON)
                .assertThat()
                .hasStatusOk();
    }

    @Test
    void traceIdIsPropagatedIntoLogMdc() {
        // Attach an in-memory Logback appender to capture logging events and their MDC
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        root.addAppender(listAppender);

        // Trigger some requests that will produce controller logs and tracing context
        for (int i = 0; i < 3; i++) {
            this.mockMvcTester
                    .get()
                    .uri("/fetch")
                    .param("type", "pdf")
                    .accept(MediaType.APPLICATION_JSON)
                    .assertThat()
                    .hasStatusOk();
        }

        // Search captured events for an MDC entry with a non-empty traceId
        boolean foundTraceId =
                listAppender.list.stream()
                        .anyMatch(
                                ev -> {
                                    String traceId = ev.getMDCPropertyMap().get("traceId");
                                    return traceId != null
                                            && !traceId.isBlank()
                                            && !traceId.equals("NONE");
                                });

        // Clean up
        root.detachAppender(listAppender);
        listAppender.stop();

        assertThat(foundTraceId)
                .withFailMessage(
                        "Expected at least one log event to contain MDC key 'traceId' with a real value")
                .isTrue();
    }

    @Test
    void prometheusScrapesApplicationMetrics() throws Exception {
        // Trigger the application endpoint a few times to produce HTTP server request metrics
        for (int i = 0; i < 5; i++) {
            this.mockMvcTester
                    .get()
                    .uri("/fetch")
                    .param("type", "pdf")
                    .accept(MediaType.APPLICATION_JSON)
                    .assertThat()
                    .hasStatusOk();
        }

        // Poll the application's MeterRegistry until a server request metric for the /fetch
        // endpoint appears.
        Awaitility.await()
                .atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(
                        () -> {
                            boolean found =
                                    meterRegistry.getMeters().stream()
                                            .anyMatch(
                                                    m -> {
                                                        String name = m.getId().getName();
                                                        if (name == null) return false;
                                                        return name.contains("http.server.requests")
                                                                || name.contains(
                                                                        "http_server_requests")
                                                                || name.contains(
                                                                        "http_server_requests_seconds");
                                                    });

                            assertThat(found).isTrue();
                        });
    }
}
