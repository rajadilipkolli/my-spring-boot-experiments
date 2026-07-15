package com.example.highrps;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(0)
public class ApiLoadBenchmark {

    private ConfigurableApplicationContext context;
    private HttpClient client;
    private final AtomicLong counter = new AtomicLong();
    private final ConcurrentHashMap<Long, Long> createdPostIds = new ConcurrentHashMap<>();
    private final AtomicLong postIdsCounter = new AtomicLong();
    private final JsonMapper jsonMapper = new JsonMapper();
    private ExecutorService executorService;

    @Setup(Level.Trial)
    public void setup() {
        context = SpringApplication.run(HighRpsApplication.class, "--spring.profiles.active=local");
        executorService = Executors.newFixedThreadPool(500);
        client = HttpClient.newBuilder().executor(executorService).build();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (context != null) {
            context.close();
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @Benchmark
    @Group("mix")
    @GroupThreads(50)
    public void createPostBenchmark() throws Exception {
        long id = counter.incrementAndGet();
        String payload = "{\"title\":\"Benchmark Post " + id
                + "\",\"content\":\"JMH content\",\"email\":\"test@example.com\",\"details\":{\"detailsKey\":\"testKey\"}}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/posts"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 300) {
            throw new RuntimeException("Request failed with status: " + response.statusCode());
        }

        try {
            JsonNode node = jsonMapper.readTree(response.body());
            if (node.has("postId")) {
                createdPostIds.put(
                        postIdsCounter.getAndIncrement(), node.get("postId").asLong());
            }
        } catch (Exception e) {
            // ignore parse errors
        }
    }

    @Benchmark
    @Group("mix")
    @GroupThreads(450)
    public void readPostBenchmark() throws Exception {
        long count = postIdsCounter.get();
        long idToRead;
        if (count == 0) {
            return; // Skip read if nothing created yet
        } else {
            long index = ThreadLocalRandom.current().nextLong(count);
            Long id = createdPostIds.get(index);
            if (id == null) {
                return;
            }
            idToRead = id;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/posts/" + idToRead))
                .GET()
                .build();

        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        // We don't fail on 404 because at the very start the post might not exist.
        if (response.statusCode() >= 500) {
            throw new RuntimeException("Read request failed with status: " + response.statusCode());
        }
    }

    @Test
    public void executeJmhRunner() throws Exception {
        Options opt = new OptionsBuilder()
                .include(ApiLoadBenchmark.class.getSimpleName())
                .forks(0)
                .build();
        new Runner(opt).run();
    }
}
