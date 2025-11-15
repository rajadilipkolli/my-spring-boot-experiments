# boot-strategy-plugin

The strategy design pattern is a behavioral design pattern that allows an object to change its behavior based on different strategies or algorithms. This pattern separates the actual behavior or algorithm from the object that uses it, so that the same object can use different strategies depending on the situation. This allows for more flexibility and reuse of code, as the object can easily switch between strategies without having to change its internal implementation.

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

### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
* Prometheus: http://localhost:9090/graph
* Grafana: http://localhost:3000/ (admin/admin)
* Navigate to http://localhost:3100/metrics to view the metrics and http://localhost:3100/ready for readiness.
