# Boot Ultimate Postgres

This module demonstrates advanced PostgreSQL features and patterns implemented in a Spring Boot application. 
The goal is a runnable application backed by PostgreSQL with schema migrations in place.

## Advanced Features

1. **UNLOGGED Cache**: A Redis-like cache backed by an `UNLOGGED` PostgreSQL table, offering TTL semantics and periodic cleanup of expired entries. Since it is unlogged, it bypasses write-ahead logging (WAL), making it extremely fast, though not crash-safe.
2. **SKIP LOCKED Job Queue**: A job queue supporting concurrent, non-blocking job claiming across multiple workers using `SELECT ... FOR UPDATE SKIP LOCKED`.
3. **LISTEN/NOTIFY Pub/Sub**: Blocking pub/sub integration using PostgreSQL's native `LISTEN` and `NOTIFY` commands, allowing for real-time inter-process communication without polling.
4. **Single-Transaction Integration**: An integration method that writes to the cache table, enqueues a job, and issues a `pg_notify`, all within a single transaction, guaranteeing that all three effects commit or roll back together.

## Running Locally

1. Start the PostgreSQL container:
   ```bash
   docker compose -f docker/docker-compose.yml up -d
   ```

2. Run the application:
   ```bash
   ./mvnw spring-boot:run -pl boot-ultimate-postgres
   ```

The `Initializer` will automatically run on startup and demonstrate adding to the cache, enqueueing a job, and publishing a pub/sub message.

## Endpoint Examples

### Cache Endpoints
- **Put**: `PUT /api/cache/{key}` (Body: `{"value":"data", "ttlMillis":60000}`)
- **Get**: `GET /api/cache/{key}`
- **Evict**: `DELETE /api/cache/{key}`

### Job Queue Endpoints
- **Enqueue**: `POST /api/queue` (Body: `{"payload":"some-task", "priority":10}`)
- **Status**: `GET /api/queue/status`

### Pub/Sub Endpoints
- **Publish**: `POST /api/pubsub/publish` (Body: `your message here`)
- **Messages**: `GET /api/pubsub/messages`
- **Combined**: `POST /api/pubsub/combined/{id}` (Body: `combined payload`)
