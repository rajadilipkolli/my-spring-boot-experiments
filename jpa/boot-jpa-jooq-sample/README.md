# boot-jpa-jooq-sample

This project demonstrates the integration of JPA and jOOQ within a Spring Boot application.

### Format code

This project uses Spotless to maintain consistent code formatting. Run the following command to format all files:

```shell
./mvnw spotless:apply
```

### Run tests

```shell
./mvnw clean verify
```

### Run locally

Ensure you have Docker and Maven installed. Then:

1. Start the required PostgreSQL database:

```shell
docker-compose -f docker/docker-compose.yml up -d
```

2. Run the application with the local profile (uses local database):

```shell
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Using Testcontainers at Development Time

Testcontainers provides isolated, throwaway instances of the PostgreSQL database for testing.
This allows you to run the application without setting up a local database.

There are two ways to run the application with Testcontainers:

You can run `TestJpaJooqApplication.java` from your IDE directly.
Alternatively, use Maven:

```shell
./mvnw spring-boot:test-run
```


### Useful Links
* [Swagger UI](http://localhost:8080/swagger-ui.html) - API documentation and testing interface
* [Actuator Endpoints](http://localhost:8080/actuator) - Application monitoring and management
