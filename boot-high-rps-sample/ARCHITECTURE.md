# ARCHITECTURE: Spring Modulithic CQRS Architecture

This application uses a modern **Spring Modulithic** approach combined with **CQRS (Command-Query Responsibility Segregation)** to achieve high performance and maintainable modularity.

## Core Architectural Principles

1.  **Spring Modulith**: The system is organized into modular domains (`author`, `post`, `postcomment`). Each module is a self-contained unit that communicates with others primarily through domain events.
2.  **CQRS**: We strictly separate the **Command** (write operations) from the **Query** (read operations) side to allow independent optimization and scaling.
3.  **Event-Driven**: Internal state changes trigger domain events, which are then externalized to Kafka for building materialized views and cross-module synchronization.
4.  **Materialized Views**: Reads are served from highly optimized materialized views in **Redis** and **Caffeine**, reducing the load on the primary PostgreSQL database.

## Architecture Overview

```text
┌───────────────────────────────────────────────────────────┐
│                    HTTP REST API LAYER                    │
│      (PostController, AuthorController, etc.)             │
└───────────────┬───────────────────────────────┬───────────┘
                ▼                               ▼
┌───────────────────────────────┐     ┌─────────────────────────────┐
│       COMMAND SIDE (Writres)  │     │      QUERY SIDE (Reads)     │
│   (PostCommandService, etc.)  │     │   (PostQueryService, etc.)  │
└───────────────┬───────────────┘     └───────────────┬─────────────┘
                │                                     │
       1. Write to DB (Transactional)                 │
       2. Publish Domain Event                        │ 1. Read from Store
                │                                     │ (Layered Strategy)
                ▼                                     ▼
┌───────────────────────────────┐     ┌─────────────────────────────┐
│    EVENT EXTERNALIZATION      │     │    MATERIALIZED VIEWS       │
│     (Kafka / Application)     │     │ (Local Cache -> Redis -> DB) │
└───────────────┬───────────────┘     └─────────────────────────────┘
                │
       3. Update Read Models
       (Async / Defer to afterCommit)
```

## Module Structure (Spring Modulith)

The application follows a modular package structure:
- `com.example.highrps.author`: Module for author management.
- `com.example.highrps.post`: Module for blog post operations.
- `com.example.highrps.postcomment`: Module for managing comments on posts.

Each module contains:
- **`command`**: Services handling write operations and publishing domain events.
- **`query`**: Services handling read operations from materialized views.
- **`domain`**: Domain-specific objects, events, and mappers.
- **`api`**: REST controllers.

## CQRS Implementation

### Command Side (Writes)
The Command side is responsible for handling state changes.
- **Service Pattern**: Uses `CommandService` (e.g., `AuthorCommandService`).
- **Validation**: Ensures that the request is valid according to business rules.
- **Transaction**: Performs atomic updates to the primary database.
- **Consistency**: Uses `TransactionSynchronizationManager` to defer cache and Redis updates until **after** the database transaction successfully commits.

### Query Side (Reads)
The Query side serves data from optimized read models.
- **Service Pattern**: Uses `QueryService` (e.g., `AuthorQueryService`).
- **Read Strategy**:
  1.  **Local Caffeine Cache**: Ultra-fast in-memory cache for hot keys.
  2.  **Redis Cache**: Distributed materialized view for broader visibility.
  3.  **PostgreSQL**: Source of truth fallback for cold data.

## Transactional Consistency

To prevent "phantom state" (where a cache is updated but the database transaction fails and rolls back), we use a deferred synchronization strategy:

```java
private void executeAfterCommit(Runnable task) {
    if (TransactionSynchronizationManager.isActualTransactionActive()) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    } else {
        task.run();
    }
}
```

This ensures that side-effects like cache invalidation, Redis updates, and tombstone markers only occur **after** a successful commit.

## Data Flow (Post Domain Example)

1.  **Create Post**: `PostCommandService` saves the post, publishes a `PostCreatedEvent` (transactional).
2.  **Event externalization**: `PostCreatedEvent` is published to Kafka.
3.  **Read Model Update**: `PostAggregatesToRedisListener` consumes the event and updates the Redis materialized view.
4.  **Query**: `PostQueryService` retrieves the post from Redis or local cache for subsequent read requests.

## How to Add a New Domain

To add a new domain (e.g., "Notification"):

1.  **Create Package**: Create `com.example.highrps.notification`.
2.  **Command Side**: Create `NotificationCommandService` and commands (`CreateNotificationCommand`).
3.  **Query Side**: Create `NotificationQueryService` and query results.
4.  **Events**: Define internal domain events (e.g., `NotificationSentEvent`).
5.  **Listener**: Add a listener to update the Notification materialized view in Redis.
6.  **Controller**: Add `NotificationController` to expose REST endpoints.

## Technology Stack

- **Java**: Version 25 (GraalVM).
- **Spring Boot**: Core framework.
- **Spring Modulith**: Structural verification and event externalization.
- **Kafka**: External event broker for consistency and materialization.
- **Redis**: Distributed read models and batch persistence queue.
- **Caffeine**: Local high-performance cache.
- **PostgreSQL**: Primary source of truth.
