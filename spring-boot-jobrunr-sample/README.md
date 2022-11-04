# spring-boot-jobrunr-sample

### Run tests

`$ ./mvnw clean verify`

### Run locally

```
$ docker-compose -f docker/docker-compose.yml up -d
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Run native

Install GraalVM 22.3 or later verify with native-image --version

```
./mvnw native:compile -Pnative
```

Building native Buildpacks `./mvnw spring-boot:build-image -Pnative`

### Useful Links

* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
* Prometheus: http://localhost:9090/
* Grafana: http://localhost:3000/ (admin/admin)
* Kibana: http://localhost:5601/
* JobRunr Dashboard: http://localhost:8000/dashboard/overview
* PgAdmin http://localhost:5050
