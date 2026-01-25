# High RPS Sample - Generic Event-Driven Architecture

## Architecture Overview

This application uses a generic event-driven architecture that can be easily extended to support multiple entity types (Post, Author, etc.) without code duplication.

## Data Flow Architecture

```
┌─────────────┐
│  Controller │  (PostController, AuthorController, etc.)
└──────┬──────┘
       │ 1. Publishes EventEnvelope
       ▼
┌─────────────────────┐
│ KafkaProducerService│
└──────┬──────────────┘
       │ 2. Sends to 'events' topic with entity type
       ▼
┌──────────────────────────┐
│ events (Generic Topic)   │  EventEnvelope{ entity: "post"|"author", payload: JSON }
└──────┬───────────────────┘
       │ 3. Kafka Streams reads and routes
       ▼
┌────────────────────────────┐
│ StreamsTopology            │
│ - eventsStream()          │  Routes by entity type
│ - postsTable()            │  Materializes to 'posts-store'
│ - authorsTable()          │  Materializes to 'authors-store'
└──────┬─────────────────────┘
       │ 4. Routes to per-entity aggregate topics
       ├─────────────────┬──────────────────┐
       ▼                 ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│posts-        │  │authors-      │  │ (future      │
│aggregates    │  │aggregates    │  │  entities)   │
└──────┬───────┘  └──────┬───────┘  └──────────────┘
       │ 5. Listeners consume aggregates
       ▼                 ▼
┌──────────────┐  ┌──────────────────────────┐
│AggregatesToRe│  │AuthorAggregatesToRedis   │
│disListener   │  │Listener                  │
└──────┬───────┘  └──────┬───────────────────┘
       │ 6. Write to cache/redis/queue
       ├─────────────────┴──────────────────┐
       ▼                                     ▼
┌─────────────┐                      ┌─────────────┐
│ Redis Cache │                      │ DB Queue    │
└─────────────┘                      └─────────────┘
```

## Read Path (Query Flow)

```
┌─────────────┐
│ GET Request │
└──────┬──────┘
       │ 1. Service.findByKey()
       ▼
┌──────────────────────┐
│ Local Caffeine Cache │  (In-memory, fast)
└──────┬───────────────┘
       │ 2. Cache miss
       ▼
┌──────────────────────┐
│ Redis                │  (Distributed cache)
└──────┬───────────────┘
       │ 3. Redis miss
       ▼
┌──────────────────────────────┐
│ Kafka Streams Interactive    │  (Materialized KTable)
│ Query (posts-store/authors-  │
│ store)                        │
└──────┬───────────────────────┘
       │ 4. Not in Streams store
       ▼
┌──────────────────────┐
│ PostgreSQL Database  │  (Source of truth)
└──────────────────────┘
```

## Batch Processing Architecture

The application uses a **generic batch processor** with a **strategy pattern** to handle asynchronous database persistence:

```
┌──────────────────────────┐
│ Aggregate Listeners      │  Write JSON + entity metadata to Redis queue
└──────┬───────────────────┘
       │ Push to queue
       ▼
┌──────────────────────────┐
│ Redis Queue              │  Single queue: events:queue
│ (events:queue)           │  Contains mixed entity types with metadata
└──────┬───────────────────┘
       │ Scheduled polling
       ▼
┌──────────────────────────────┐
│ ScheduledBatchProcessor      │  Generic coordinator
│ - Parses entity type         │
│ - Groups by entity type      │
│ - Routes to processors       │
└──────┬───────────────────────┘
       │ Delegates to entity-specific processors
       ├─────────────────┬──────────────────┐
       ▼                 ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│PostBatch     │  │AuthorBatch   │  │(Future)      │
│Processor     │  │Processor     │  │CommentBatch  │
│              │  │              │  │Processor     │
└──────┬───────┘  └──────┬───────┘  └──────────────┘
       │ Batch ops       │ Batch ops
       ▼                 ▼
┌──────────────┐  ┌──────────────┐
│PostRepository│  │AuthorRepo    │
└──────────────┘  └──────────────┘
```

### EntityBatchProcessor Strategy
Each entity type has its own processor implementing the `EntityBatchProcessor` interface:
- **getEntityType()**: Returns entity identifier ("post", "author")
- **processUpserts(payloads)**: Batch persist entities
- **processDeletes(keys)**: Batch delete entities
- **extractKey(payload)**: Extract entity key from JSON

This design allows adding new entities by simply creating a new processor component.

## Components

### 1. Generic Event Envelope
**File**: `EventEnvelope.java`
```java
public record EventEnvelope(String entity, JsonNode payload)
```
- Wraps any entity payload with an entity type label
- Allows single `events` topic to handle all entity types
- Enables dynamic routing without hardcoded logic

### 2. Kafka Producer Service
**File**: `KafkaProducerService.java`
- **publishEnvelope(entity, key, payload)**: Wraps payload in EventEnvelope and publishes to `events` topic
- **publishDeleteForEntity(entity, key)**: Sends tombstone to per-entity aggregate topic
- Generic design: works for any entity type

### 3. Streams Topology
**File**: `StreamsTopology.java`

#### eventsStream() Bean
- Reads from `events` topic (EventEnvelope)
- Filters by entity type ("post", "author", etc.)
- Converts payload to typed object (NewPostRequest, AuthorRequest)
- Routes to per-entity aggregate topics
- **Returns KStream** (fixes void return type error)

#### postsTable() Bean
- Creates materialized KTable from `posts-aggregates` topic
- Store name: `posts-store`
- Enables interactive queries in PostService

#### authorsTable() Bean
- Creates materialized KTable from `authors-aggregates` topic
- Store name: `authors-store`
- Enables interactive queries in AuthorService

### 4. Aggregate Listeners
**Files**: `AggregatesToRedisListener.java`, `AuthorAggregatesToRedisListener.java`

- Consume from per-entity aggregate topics
- Handle both upserts (payload) and deletes (null/tombstone)
- Write to Redis cache for fast reads
- Enqueue to Redis list for async DB persistence (single shared queue)
- Implement DLT (Dead Letter Topic) handlers for error recovery

### 6. Batch Processing (Async DB Persistence)
**Files**: `ScheduledBatchProcessor.java`, `EntityBatchProcessor.java`, `PostBatchProcessor.java`, `AuthorBatchProcessor.java`

**ScheduledBatchProcessor** (Generic Coordinator):
- Polls Redis queue on fixed delay
- Parses entity type from payload metadata (`__entity` field or inferred from structure)
- Groups items by entity type
- Deduplicates by entity key within each type
- Routes to appropriate EntityBatchProcessor

**EntityBatchProcessor** (Strategy Interface):
- Defines contract for entity-specific batch operations
- Each entity type implements this interface

**PostBatchProcessor** / **AuthorBatchProcessor**:
- Maps JSON payloads to entities
- Performs batch repository operations
- Handles errors with logging

### 5. Services
**Files**: `PostService.java`, `AuthorService.java`

- Publish events via KafkaProducerService
- Implement layered read strategy: Cache → Redis → Streams → DB
- Manage local Caffeine cache for ultra-fast reads
- Query Kafka Streams stores for recent data
- Fallback to DB when needed

### 7. Kafka Configuration
**File**: `KafkaConfig.java`

- **ProducerFactory**: Generic Object-valued producer
- **Consumer Factories**: Type-specific consumers for each entity
  - `newPostKafkaListenerContainerFactory`
  - `authorKafkaListenerContainerFactory`
- **Topics**: Creates `events`, `posts-aggregates`, `authors-aggregates`

## How to Add a New Entity

To add a new entity (e.g., "Comment"):

1. **Create Request/Response DTOs and Mappers**
   ```java
   public record CommentRequest(String id, String text, String authorEmail) {}
   public record CommentResponse(String id, String text, ...) {}
   ```

2. **Add Mapper Interfaces**
   ```java
   @Mapper(componentModel = SPRING)
   public interface CommentRequestToResponseMapper {
       CommentResponse mapToCommentResponse(CommentRequest request);
   }
   ```

3. **Update StreamsTopology**
   ```java
   // In eventsStream() bean, add filter and route
   KStream<String, CommentRequest> comments = envelopeStream
       .filter((k, env) -> "comment".equalsIgnoreCase(env.entity()))
       .mapValues(env -> mapper.convertValue(env.payload(), CommentRequest.class));
   comments.to("comments-aggregates", Produced.with(Serdes.String(), commentSerde));
   
   // Add new KTable bean
   @Bean
   public KTable<String, CommentRequest> commentsTable(StreamsBuilder builder) {
       return builder.table("comments-aggregates", 
           Consumed.with(Serdes.String(), commentSerde),
           Materialized.as("comments-store"));
   }
   ```

4. **Add Listener**
   ```java
   @Component
   public class CommentAggregatesToRedisListener {
       @KafkaListener(topics = "comments-aggregates",
                      containerFactory = "commentKafkaListenerContainerFactory")
       public void handleAggregate(ConsumerRecord<String, CommentRequest> record) {
           // Same pattern as Post/Author listeners
       }
   }
   ```

5. **Update KafkaConfig**
   ```java
   @Bean
   ConsumerFactory<String, CommentRequest> commentConsumerFactory(...) { ... }
   
   @Bean
   ConcurrentKafkaListenerContainerFactory<String, CommentRequest> 
       commentKafkaListenerContainerFactory(...) { ... }
   ```

6. **Create Service**
   ```java
   @Service
   public class CommentService {
       // Inject same collaborators as PostService/AuthorService
       // Use kafkaProducerService.publishEnvelope("comment", key, payload)
   }
   ```

7. **Create Batch Processor**
   ```java
   @Component
   public class CommentBatchProcessor implements EntityBatchProcessor {
       @Override
       public String getEntityType() { return "comment"; }
       
       @Override
       public void processUpserts(List<String> payloads) {
           // Parse JSON, map to entities, save to repository
       }by creating a single batch processor class
2. **Performance**: Multi-layer caching (Local → Redis → Streams → DB) + Batch DB writes
3. **Scalability**: Kafka Streams horizontal scaling + Single queue for all entities
4. **Reliability**: Tombstone handling, DLT for errors, idempotent consumers, re-queuing on failure
5. **Consistency**: Single source of truth pattern with eventual consistency
6. **Observability**: Structured logging at each layer with entity type context
7. **Generic Design**: Strategy pattern allows heterogeneous entity processing without code duplication
       
       @Override
       public String extractKey(String payload) {
           // Extract comment ID from JSON
       }
   }
   ```

8. **Create Controller**
   ```java
   @RestController
   public class CommentController {
       // Standard REST endpoints calling CommentService
   }
   ```

**That's it!** The `ScheduledBatchProcessor` will automatically discover and use the new `CommentBatchProcessor` through Spring's dependency injection.

## Key Benefits

1. **Extensibility**: Add new entities without modifying existing code
2. **Performance**: Multi-layer caching (Local → Redis → Streams → DB)
3. **Scalability**: Kafka Streams horizontal scaling
4. **Reliability**: Tombstone handling, DLT for errors, idempotent consumers
5. **Consistency**: Single source of truth pattern with eventual consistency
6. **Observability**: Structured logging at each layer

## Configuration Properties

```properties
# Kafka topics
app.kafka.events-topic.partitions=3
app.kafka.events-topic.replication-factor=1
app.kafka.posts-aggregates-topic.partitions=3
app.kafka.posts-aggregates-topic.replication-factor=1
app.kafka.authors-aggregates-topic.partitions=3
app.kafka.authors-aggregates-topic.replication-factor=1

# Cache and queue
app.batch.queue-key=events:queue
```

## Testing the Architecture

1. **Create a Post**:
   ```bash
   POST /posts
   {
     "title": "test-post",
     "content": "Hello World",
     "email": "test@example.com"
   }
   ```

2. **Create an Author**:
   ```bash
   POST /authors
   {
     "firstName": "John",
     "lastName": "Doe",
     "email": "john@example.com",
     "mobile": 1234567890
   }
   ```

3. **Verify Flow**:
   - Check Kafka topics: `events`, `posts-aggregates`, `authors-aggregates`
   - Check Redis keys: `posts:test-post`, `authors:john@example.com`
   - Check DB for persisted records
   - Query APIs to verify cache hits

## Troubleshooting

- **"needs to have a non-void return type"**: Fixed by returning KStream from eventsStream()
- **No interactive query store**: Ensure KTable beans (postsTable, authorsTable) are created
- **Consumer not receiving messages**: Check containerFactory names match @KafkaListener
- **Tombstone not deleting**: Verify publishDeleteForEntity sends to correct aggregate topic
