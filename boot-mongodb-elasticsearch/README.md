# Spring Boot Reactive MongoDB and Elasticsearch Integration

A reactive Spring Boot application demonstrating seamless integration between MongoDB and Elasticsearch, featuring real-time search capabilities including geospatial queries, full-text search, and aggregations.

---

## Highlights

- **Reactive Streams Approach**: Uses Spring Data’s reactive driver for both MongoDB and Elasticsearch.
- **Advanced Queries**: Demonstrates geospatial searches, text-indexed searches, and filtering in near real-time.
- **Scalable Architecture**: Docker-compose-based environment for local testing.

---

## Architecture

The application follows a reactive architecture pattern using Spring WebFlux:

```mermaid
flowchart TB
    %% Definitions
    Client([Client / Web Browser])
    
    subgraph SpringBoot [Spring Boot Application]
        direction TB
        Controllers[WebFlux Controllers\nRestaurant / Search API]
        Services[Business Logic Services\nReactive Mono/Flux]
        
        subgraph DataAccess [Data Access Layer]
            MongoRepo[(MongoDB \n Reactive Repo)]
            ESRepo[(Elasticsearch \n Reactive Repo)]
            Listener[ChangeStream\nStartup Listener]
        end
        
        Controllers --> |Process Request| Services
        Services --> |Save Primary Data| MongoRepo
        Services --> |Execute Search| ESRepo
        Listener -.-> |Sync Docs| ESRepo
    end
    
    subgraph Infrastructure [Data Infrastructure]
        direction LR
        MongoDB[(MongoDB\nReplica Set)]
        Elastic[(Elasticsearch 8.x)]
        
        MongoDB -.-> |Emit Change Streams| Listener
    end
    
    %% Connections
    Client <--> |HTTP/REST| Controllers
    MongoRepo <--> |Reactive Driver| MongoDB
    ESRepo <--> |Reactive client| Elastic
    
    classDef primary fill:#e1f5fe,stroke:#01579b,stroke-width:2px;
    classDef secondary fill:#f3e5f5,stroke:#4a148c,stroke-width:2px;
    classDef db fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px;
    
    class SpringBoot primary;
    class Infrastructure secondary;
    class MongoDB,Elastic db;
```
---

## Key Features
- Reactive API endpoints using Spring WebFlux and reactive drivers
- **Real-time Synchronization**: MongoDB change streams to Elasticsearch
- MongoDB for primary data storage
- Advanced Search Capabilities:
  - Full-text search
  - Geospatial queries
  - Aggregation operations
- Comprehensive validation
- Exception handling
- **API Documentation**: OpenAPI/Swagger UI integration

---

## Sequence Diagrams

### Restaurant Creation Flow
```mermaid
sequenceDiagram
    participant C as Client
    participant RC as RestaurantController
    participant RS as RestaurantService
    participant MR as MongoRepository
    participant ER as ESRepository
    
    C->>RC: POST /api/restaurant
    RC->>RC: Validate Request
    RC->>RS: createRestaurant()
    RS->>MR: save()
    MR-->>RS: Restaurant
    RS->>ER: index()
    ER-->>RS: Success
    RS-->>RC: Restaurant
    RC-->>C: 201 Created
```

### Search Operation Flow
```mermaid
sequenceDiagram
    participant C as Client
    participant SC as SearchController
    participant SS as SearchService
    participant ER as ESRepository
    
    C->>SC: GET /api/search
    SC->>SC: Validate Parameters
    SC->>SS: search()
    SS->>ER: search()
    ER-->>SS: SearchResults
    SS-->>SC: SearchPage
    SC-->>C: 200 OK
```
---

## Data Flow

```mermaid
flowchart LR
    Client --> RestController
    RestController --> Service
    Service --> MongoDB
    MongoDB --> ChangeStream
    ChangeStream --> Elasticsearch
    Service --> Elasticsearch
```
---
## Configuration Notes

### MongoDB
- Uses replica set for change streams
- Transaction support with `ReactiveMongoTransactionManager`
- Default database: mongoes

## Getting Started

### Prerequisites
- JDK 25+
- Docker and Docker Compose
- Maven 3.9+

---

### Running Locally
1. Start the infrastructure:
```bash
docker compose -f docker/docker-compose.yml up -d
```

2. Run the application:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Running Tests
```bash
./mvnw clean verify
```

---

## API Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs

## Monitoring
- Actuator: http://localhost:8080/actuator
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

## Development Tools
- Kibana: http://localhost:5601/app/kibana#/dev_tools/console?_g=()
- Elasticsearch: http://localhost:9200
- Mongo Express: http://localhost:8081

## Useful Elasticsearch Commands
- Count documents: `GET /restaurant/_count`
- Search all: `GET /restaurant/_search`
- View mapping: `GET /restaurant/_mapping`

## Configuration Properties
Key application properties:
```properties
spring.mongodb.database=mongoes
spring.mongodb.uri=mongodb://localhost:27017/mongoes?replicaSet=rs0&readPreference=primary&directConnection=true
spring.elasticsearch.uris=localhost:9200
spring.elasticsearch.socket-timeout=10s
```

## Exception Handling
The application includes global exception handling for:
- Validation errors
- Duplicate entries
- Resource not found
- General server errors

### Reference
- https://medium.com/geekculture/elastic-search-queries-hands-on-examples-fe5b2bc10c0e
