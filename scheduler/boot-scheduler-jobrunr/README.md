# boot-scheduler-jobrunr

JobRunr is a job scheduling and execution platform that enables businesses to automate and manage background tasks and processes. It allows users to schedule jobs to run at a specific time, on a recurring schedule, or in response to a trigger event, and provides tools for monitoring and managing the execution of these jobs. JobRunr can be integrated with a variety of systems and technologies, including databases, message brokers, and cloud services

### Run tests

```shell
./mvnw clean verify
```

### Run locally

```shell
docker-compose -f docker/docker-compose.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Run native

Install GraalVM 22.3 or later verify with native-image --version

```shell
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
