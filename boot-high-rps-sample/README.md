High RPS Spring Boot 4 Sample

This module implements the architecture blueprint from "100,000 RPS with Spring Boot 4" (2025 edition).

Architecture overview

Client → CDN/LB → Spring Boot 4 (virtual threads) → Redis (materialized views) → Kafka (event pipeline) → Async DB writer

Key components implemented in this module

- Virtual threads enabled via `spring.threads.virtual.enabled=true` (blocking code runs on cheap virtual threads).
- Hot-path reads: `HelloController` → `HelloService` reads from an in-memory Caffeine LRU cache, then Redis (`stats:{id}`).
- Event pipeline: producers publish `EventDto` to Kafka `events` topic.
- Kafka Streams topology aggregates the latest value per key and writes aggregates to `stats-aggregates` topic.
- Materialized view writer: `AggregatesToRedisListener` consumes `stats-aggregates`, writes idempotently into Redis and uses Spring Retry with DLQ fallback.
- Async DB writes: `ScheduledAggregatesConsumer` polls `stats-aggregates` and batches writes to the JPA repository (H2 for local testing).
- Observability: Actuator + Micrometer Prometheus registry expose `/actuator/prometheus`.

Run the smoke test (docker-compose + app)

1. Start local infra (Zookeeper, Kafka, Redis):

```powershell
cd boot-high-rps-sample
docker compose up -d
```

2. Build the app:

```powershell
mvn -DskipTests package
```

3. Run the app with the production profile (uses `kafka` and `redis` hostnames from the compose network):

```powershell
java -jar target/boot-high-rps-sample-0.1.0-SNAPSHOT.jar --spring.profiles.active=production
```

4. Verify the Prometheus metrics endpoint:

```powershell
curl http://localhost:8080/actuator/prometheus
```

You should see Micrometer metrics like `jvm_memory_used_bytes`, `process_cpu_usage`, and `http_server_requests_seconds_count`.

Notes and tuning

- Use ZGC (`-XX:+UseZGC`) and tune heap (1–2GB) for production.
- Ensure Redis is provisioned with sufficient connections and low-latency (Lettuce pool is configured in `application-production.properties`).
- Kafka Streams can be scaled to multiple instances; ensure the aggregates topic has adequate partitions.

If you want, I can run the smoke test now and verify `/actuator/prometheus` — tell me to proceed.

Troubleshooting Kafka Streams (Offset/State issues)

- If you see startup errors mentioning "OffsetOutOfRangeException" or "Tasks are corrupted", it means local state or changelog offsets don't match the broker.
- For development, you can clear local state and reset the Streams application by:

```powershell
# stop the app
Remove-Item -Recurse -Force .\kafka-streams-state
# restart the app; the Streams app will recreate local state and restore from changelog topics
java -jar target/boot-high-rps-sample-0.1.0-SNAPSHOT.jar
```

- Alternatively, enable the following development-safe properties in `src/main/resources/application.properties` (already set in this module):

```
spring.kafka.streams.properties.auto.offset.reset=earliest
spring.kafka.streams.state-dir=./kafka-streams-state
spring.kafka.streams.clean-up=true
```

- For production, do NOT enable `clean-up` — instead use controlled application resets and repartitioning strategies.

Changes made by this update

- Fixed `KafkaConsumerService` to use the correct Jackson `ObjectMapper` import.
- Exposed a `KStream<String, EventDto>` bean in `StreamsTopology` and enabled Kafka Streams auto-configuration so Spring Boot manages the Streams lifecycle.
- Added development-friendly Kafka Streams properties to `application.properties` to ease local debugging of offset/state issues.

If you'd like, I can also add a small script to run an end-to-end smoke test that produces events to `events` topic and validates Redis entries. Ask and I'll add it.

Postgres and Prometheus

To run with PostgreSQL and Prometheus locally use the updated compose file:

```powershell
cd boot-high-rps-sample
docker compose up -d

# build and run the app (production profile uses postgres:5432 hostname)
mvn -DskipTests package
java -jar target/boot-high-rps-sample-0.1.0-SNAPSHOT.jar --spring.profiles.active=production --spring.kafka.bootstrap-servers=localhost:29092 --spring.data.redis.host=localhost
```

Prometheus will be available at http://localhost:9090 and is configured to scrape the Spring Boot `/actuator/prometheus` endpoint via `host.docker.internal:8080`.

Recommended JVM flags (example for production container):

```
-XX:+UseZGC -Xms1g -Xmx2g -XX:+UseCompressedOops -XX:MaxDirectMemorySize=512m
```
