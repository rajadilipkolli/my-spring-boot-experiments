# Spring Boot jOOQ R2DBC Sample

Demonstrates reactive SQL operations using [jOOQ](https://www.jooq.org/) and the [R2DBC](https://r2dbc.io/) connection standard in a Spring Boot app.

## Features

1. **Non-Blocking I/O**: Uses reactive driver for asynchronous query handling.
2. **jOOQ Code Generation**: Strongly typed DSL for building queries at compile time.
3. **Native Integration** with Spring Boot dev tools (liquibase/flyway optional).

---

### Run tests
`./mvnw clean verify`

### Run locally
```shell
docker-compose -f docker/docker-compose.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```


### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator


### Notes
 * When using `RouterFunction` ordering is important
 * Postgres `uuid-ossp` extension is used for autogenerating functions for uuid
