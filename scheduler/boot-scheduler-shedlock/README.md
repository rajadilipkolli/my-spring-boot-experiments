# Boot Scheduler with ShedLock

Ensures only one instance of a scheduled job runs in a clustered environment, preventing overlapping tasks across multiple nodes.

---

## Main Benefits

1. **Fail-safe**: If one node fails mid-job, another node can acquire the lock after lease expiry.
2. **Database Agnostic**: Works with multiple DB types.

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
