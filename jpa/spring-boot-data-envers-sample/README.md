# spring-boot-data-envers-sample

Spring Data Envers is a tool for auditing changes made to persistent entities in a Spring-based application. It allows developers to track and log changes made to entities over time, providing a historical view of the data. This can be useful for compliance and regulatory purposes, as well as for debugging and performance analysis. Spring Data Envers integrates with the Hibernate Envers library and offers a convenient and declarative way to enable auditing in a Spring application.

### Run tests

`./gradlew clean build`

### Run locally

```shell
docker-compose -f docker/docker-compose.yml up -d
./gradlew bootRun -Plocal
```

### Useful Links

* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
* Grafana : http://localhost:3000 (admin/admin)
* Prometheus : http://localhost:9000/graph

### Update to Spring Boot 3

* Removed problem-spring-web-starter and replaced with Spring OOTB `spring.mvc.problemdetails.enabled=true`
* Migrated from javax namespace to jakarta namespace
* By Default `hibernate\_sequence` is not used instead `tablename\_seq` is used while mapping sequence name and `pooled-lo` generation strategy is used OOTB
