# Multiple Datasource's with Parallel Data Fetching

This Spring Boot application demonstrates how to work with multiple datasources and implement efficient parallel data fetching.

---

## Features

- Multiple database connections (PostgreSQL and MySQL)
- Parallel data fetching using CompletableFuture
- Database migrations using both Liquibase and Flyway
- Exception handling with Problem Details
- Virtual Threads support
- Swagger API documentation

---

## Architecture

### Database Setup
- **PostgreSQL Database**: Stores member information
    - Managed by Liquibase migrations
    - Uses sequence-based ID generation
- **MySQL Database**: Stores cardholder information
    - Managed by Flyway migrations
    - Uses identity-based ID generation

### Key Components

- **DetailsService**: Implements parallel data fetching
    - Uses CompletableFuture for asynchronous operations
    - Implements timeout handling (5 seconds)
    - Provides comprehensive error handling
- **Data Models**:
    - `Member`: Core member information (PostgreSQL)
    - `CardHolder`: Card-related information (MySQL)
- **Exception Handling**:
    - `CustomServiceException`: For service-layer errors
    - `MemberNotFoundException`: For missing member scenarios
    - Problem Details support for standardized error responses

## Configuration

### PostgreSQL Configuration
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/memberdb
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### MySQL Configuration
```properties
app.datasource.cardholder.url=jdbc:mysql://localhost:3306/cardholderdb
app.datasource.cardholder.username=user
app.datasource.cardholder.password=password
```

## Getting Started

1. Start PostgreSQL and MySQL databases
2. Configure database connections in `application.properties`
3. Run the application: The databases will be automatically initialized
4. Access Swagger UI: http://localhost:8080/swagger-ui.html

## Implementation Notes

- Database migrations run automatically on startup
- Data bootstrapping occurs via `ApplicationReadyEvent`
- Parallel data fetching implemented using Spring's @Qualifier("applicationTaskExecutor")
- Virtual Threads enabled for improved scalability
- HikariCP connection pooling with optimized settings
- Automatic commit disabled for better transaction control

## API Endpoints

- `GET /api/details/{memberId}`: Fetches member details from both databases in parallel
    - Returns combined information from both datasource's
    - Implements timeout handling (5 seconds)
    - Returns standardized error responses using Problem Details

## Testing

- Integration tests demonstrate concurrent request handling
- MockMvcTester used for API testing
- Testcontainers with parallel startup for database testing
