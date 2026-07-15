# High-RPS Blueprint Implementation (boot-high-rps-sample)

This module implements a simplified version of the "100,000 RPS with Spring Boot 4" blueprint.

Summary
- Virtual Threads enabled (Spring Boot 4).
- API hot path reads from Redis and local Caffeine cache only.
- Kafka used for async event pipeline; Kafka Streams performs pre-aggregation and materialized views.
- Redis stores materialized aggregates and is the single source of truth for reads.
- PostgreSQL (or other DB) is used for async durability/writes and is not on the hot path.

Architecture mapping to this project
- Controller: `PostController` — lightweight synchronous handlers running on virtual threads.
- Producer: `KafkaProducerService` — publishes `EventDto` to topic `events`.
- Streams: `StreamsTopology` — consumes `events`, reduces latest `value` per key, materializes persistent store `posts-store` (String), and emits `posts-aggregates` as strings.
- Materializer: `AggregatesToRedisListener` — consumes `posts-aggregates` and writes JSON into `posts:{id}` Redis keys.
- API fallback: `PostService` — cache → Redis → Kafka Streams interactive query (`posts-store`).
- DB writer: `ScheduledBatchProcessor` — pops from `events:queue` and persists to DB asynchronously.

Kafka topics used
- `events` — raw events (EventDto JSON). Produced by APIs when publishing events.
- `posts-aggregates` — string aggregates emitted by Kafka Streams. Consumed by Redis materializer.

Do we need 3 topics?

Currently this module uses 2 application-level topics: `events` and `posts-aggregates`.
Kafka Streams will create internal changelog topics for state stores automatically. The blueprint sometimes describes a third topic for pre-aggregation or durable event storage, but in practice the Streams changelog covers that need. If you want a dedicated changelog-like topic for manual inspection or a separate compaction policy, you can add it, but it's not required for correctness.

Redis keys
- `posts:{id}` — materialized JSON aggregate used by API reads.
- `events:queue` — list used for batch DB writes (pushed on publish).

Run & smoke test (local)
1. Start infra (docker compose):

```powershell
docker compose up -d kafka redis postgres
```

2. Build and run the app:

```powershell
cd boot-high-rps-sample
mvn -DskipTests package
mvn spring-boot:run
```

3. Publish an event:

```powershell
curl http://localhost:8080/publish/user-123
# or
curl -X POST -H "Content-Type: application/json" -d '{"id":"user-123","value":42}' http://localhost:8080/events
```

4. Verify Redis and API:

```powershell
redis-cli GET posts:user-123
redis-cli LRANGE events:queue 0 -1
curl http://localhost:8080/posts/user-123
```

Production tuning notes
- Enable virtual threads: `spring.threads.virtual.enabled=true` (already present).
- Redis: use Lettuce with high pool sizes, tune `max-active`, `max-wait`, and timeouts for your workload.
- Kafka: consider compacted topics for long-lived aggregate streams; monitor state store restorations and tune partitions.
- Kafka Streams: ensure sufficient partitions and state-store placement to handle load.
- JVM: prefer ZGC for low pause times; size heap conservatively.

Suggestions & next steps
- Add an HTTP readiness probe that confirms `posts-store` is queryable before serving interactive queries.
- Add observability: meters for `posts-store` misses, Streams state, Redis RTT, and batch processing throughput.
- Harden error handling in Redis materializer (retry/backoff, DLQ monitoring).

