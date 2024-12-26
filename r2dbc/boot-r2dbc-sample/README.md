# Reactive Application with R2DBC

Shows how to build reactive CRUD operations in Spring Boot using the R2DBC specification for non-blocking database connectivity.

---

## Why R2DBC?

- **Non-Blocking I/O**: Great for high-throughput applications needing concurrency.
- **Built for Reactive Streams**: Perfectly aligns with Spring WebFlux.

---

### Run tests

`./mvnw clean verify`

### Run locally

```shell
 docker-compose -f docker/docker-compose.yml up -d
 ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
### Using Testcontainers at Development Time
You can run `TestApplication.java` from your IDE directly.
You can also run the application using Maven as follows:

```shell
./mvnw spotless:apply spring-boot:test-run
```


### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
