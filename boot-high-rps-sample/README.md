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

Currently this module uses 4 application-level topics: `events`, `posts-aggregates`, `authors-aggregates`, and `post-comments-aggregates`.
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

3. Create an author and publish a post:

```powershell
curl -X POST -H "Content-Type: application/json" -d '{"email":"test@local.com","firstName":"John","lastName":"Doe","mobile":1234567890}' http://localhost:8080/api/author

curl -X POST -H "Content-Type: application/json" -d '{"title":"High RPS","content":"Testing throughput","email":"test@local.com","published":true}' http://localhost:8080/api/posts
```

4. Verify Redis and API:

```powershell
# Get the postId from the response of the previous command
redis-cli GET posts:<postId>
curl http://localhost:8080/api/posts/<postId>
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

## Throughput Benchmarks (ApiLoadBenchmark)
We ran JMH benchmarks on the local environment simulating a workload of 90% read threads and 10% write threads.

| Metric                                                        | Total ops/s    | Read ops/s     | Write ops/s      |
|---------------------------------------------------------------|----------------|----------------|------------------|
| Before (100 Threads)                                          | ~867 ops/s     | ~762 ops/s     | ~104 ops/s       |
| After Async Refactoring (100 Threads)                         | ~867 ops/s     | ~762 ops/s     | ~104 ops/s       |
| After Async Refactoring (500 Threads)                         | ~825 ops/s     | ~739 ops/s     | ~85 ops/s        |
| After Removing Redis Sync Writes & Batch Tuning (500 Threads) | ~819 ops/s     | ~591 ops/s     | ~227 ops/s       |

**Note on Redis Sync Optimization:** By eliminating redundant, blocking network I/O calls to Redis from the API hot-path (and delegating them fully to background Kafka Streams consumer event loops), the application achieves a **~2.6x increase in write-throughput concurrency** (227 ops/s up from 85 ops/s) under extreme load (500 threads).

**Key Takeaways:**
- **Zero-Serialization Reads**: Utilizing a multi-layered local Caffeine cache in combination with Redis and Kafka Streams State Stores allows `GET` queries to bypass JSON serialization overhead entirely. The read throughput achieves native memory-like speed.
- **N+1 Optimization**: During heavy load (500 concurrent connections), the background Kafka-to-PostgreSQL batch processors initially timed out. Pre-extracting aggregate data and doing singular bulk queries (`findByEmailInAllIgnoreCase`, `findByTagNameInAllIgnoreCase`) solved the issue, eliminating all `BatchUpdateException` errors.
- The 100-thread setup is the sweet spot for maximizing JMH throughput locally before Tomcat and the JVM experience excessive context switching overhead.
