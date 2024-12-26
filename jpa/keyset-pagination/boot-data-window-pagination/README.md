# Boot Data Window Pagination

Illustrates keyset (a.k.a. window) pagination using Spring Data JPA to handle large data sets efficiently without offset-based pagination.

---

## Why Keyset Pagination?

- **Performance**: More efficient than offset pagination for large, frequently updated datasets.
- **Consistency**: Avoids certain anomalies when new data is inserted or old data is removed between requests.

---

### Format code

```shell
./mvnw spotless:apply
```

### Run tests

```shell
./mvnw clean verify
```

### Run locally

```shell
docker-compose -f docker/docker-compose.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Using Testcontainers at Development Time
You can run `TestApplication.java` from your IDE directly.
You can also run the application using Maven as follows:

```shell
./mvnw spring-boot:test-run
```


### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
