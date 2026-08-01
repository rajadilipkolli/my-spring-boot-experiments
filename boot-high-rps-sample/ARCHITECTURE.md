# ARCHITECTURE: High-Throughput Event-Driven CQRS

This application (`boot-high-rps-sample`) demonstrates an ultra-high-throughput, event-driven CQRS architecture. It completely decouples the synchronous HTTP command path from the relational database to achieve maximum performance and resilience.

## Core Architectural Principles

1. **No Synchronous DB Blocking**: The command path (write operations) strictly avoids writing to PostgreSQL synchronously.
2. **Kafka as the Immediate Ledger**: State mutations are validated and published directly to Kafka (`acks=all`, idempotence enabled) as the immediate durable ledger.
3. **At-Least-Once Materialization**: Kafka listeners consume events, hand them off to Redis Streams (`XADD`) for durable queueing, and acknowledge the Kafka offset only after the Redis write succeeds.
4. **Bulk Upsert Batching**: Scheduled processors read from Redis Streams (`XREADGROUP`), execute natural-key upserts against PostgreSQL in bulk, and acknowledge (`XACK`) only after the DB transaction commits.

## Architecture Data Flow

```mermaid
flowchart TD
    Client((Client)) -->|1. HTTP POST| REST(REST Controllers)
    REST -->|2. Validate & Command| Service(Command Services)
    Service -->|3. Produce Event| KafkaRaw[(Kafka: events topic)]
    
    KafkaRaw -->|4. Consume| KStreams(Kafka Streams)
    KStreams -->|5. Aggregate| KafkaAgg[(Kafka: *-aggregates topics)]
    
    KafkaAgg -->|6. Consume| Listener(Kafka Listeners)
    Listener -->|7. XADD| RedisStreams[(Redis Streams)]
    
    RedisStreams -->|8. XREADGROUP| Batch(Scheduled Batch Processors)
    Batch -->|9. Bulk Upsert| Postgres[(PostgreSQL)]
    Batch -.->|10. XACK on Commit| RedisStreams
```

## Detailed CQRS Implementation

### Command Side (Writes)
The Command side handles state changes at thousands of RPS by bypassing the database.
- **Service Pattern**: Commands (e.g., `AuthorCommandService`) acquire distributed reservations (Redis) to prevent duplicate creation, then immediately publish domain events (e.g., `AuthorCreatedEvent`) directly to Kafka.
- **Resilience**: Bounded publish timeouts prevent thread exhaustion if brokers fail. The system gracefully returns HTTP 503 instead of 500 when Kafka is unreachable (`KafkaPublishPendingException`).

### Query Side (Reads)
The Query side serves data from highly optimized read models.
- **Layered Caching Strategy**:
  1. **Caffeine**: Ultra-fast, localized in-memory cache.
  2. **Redis**: Distributed materialized view (updated directly by the Kafka-to-Redis listeners).
  3. **PostgreSQL**: Source of truth fallback and interactive query target.
- **Interactive Queries**: Kafka Streams interactive queries are utilized for global state lookups.

## Fault Tolerance & Reliability Patterns

1. **Dead Letter Queues (DLQ)**: Deserialization failures (via `ErrorHandlingDeserializer`) and business-logic errors automatically route to a resilient Redis-backed DLQ system.
2. **Circuit Breakers**: `Resilience4j` Circuit Breakers and Bulkheads wrap the Redis projections and database batch writes to halt processing gracefully during backend degradation.
3. **Idempotency via Natural Keys**: Because `BatchProcessor` operations use natural-key `upserts`, redelivery caused by a crash between the database write (Step 9) and the Redis `XACK` (Step 10) is completely safe.
4. **Graceful Shutdown & Offsets**: Auto-commit is disabled (`enable-auto-commit=false`). Manual acks are only issued *after* the durable handoff, ensuring zero data loss during rebalances.

## Observability

- **MDC Propagation**: Correlation IDs are generated at the HTTP layer (`CorrelationIdFilter`) and propagated natively across thread and network boundaries as Kafka headers via an `MdcProducerInterceptor`.
- **OpenTelemetry (OTel)**: Metrics, consumer lags, and distributed traces are seamlessly exported to a Grafana/Prometheus (LGTM) stack.
