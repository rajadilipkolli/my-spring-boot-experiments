# Spring Data Envers

Extends Spring Data JPA with [Hibernate Envers](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#envers) to audit all changes to your entities automatically.

## Why Use Envers?

- **Compliance & Audit**: Track who changed what and when.
- **Version History**: Revert or compare entity versions over time.


Spring Data Envers is a tool for auditing changes made to persistent entities in a Spring-based application. It allows developers to track and log changes made to entities over time, providing a historical view of the data. This can be useful for compliance and regulatory purposes, as well as for debugging and performance analysis. Spring Data Envers integrates with the Hibernate Envers library and offers a convenient and declarative way to enable auditing in a Spring application.

---

## Additional Notes
* By default, Envers creates dedicated audit tables with _AUD suffix.
* If you need custom revision listeners, see the RevisionListener interface.

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
* Grafana : http://localhost:3000 (admin/admin)
* Prometheus : http://localhost:9090/graph
* Alerts: http://localhost:9090/alerts
* Alerts Stats: http://localhost:9093/

### Update to Spring Boot 3

* Removed problem-spring-web-starter and replaced with Spring OOTB `spring.mvc.problemdetails.enabled=true`
* Migrated from javax namespace to jakarta namespace
* By Default `hibernate_sequence` is not used instead `tablename_seq` is used while mapping sequence name and `pooled-lo` generation strategy is used OOTB

### Reference:
 
 * Alerts - https://github.com/emredmrcan/tutorials/tree/main/monitoring
 * Auditing Strategy - https://vladmihalcea.com/the-best-way-to-implement-an-audit-log-using-hibernate-envers/
