# Custom Sequence Generation

Custom sequence in Hibernate refers to the ability to specify a custom sequence generator for generating unique values for the primary keys of entities. This allows users to define their own sequence generation strategy, rather than relying on the default strategy provided by Hibernate. Custom sequence generators can be useful for ensuring that primary keys are generated in a specific format or based on specific criteria.

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

### Reference

* https://vladmihalcea.com/lazyconnectiondatasourceproxy-spring-data-jpa/

### Notes
* MariaDb doesn't support pooled-lo algorithm for fetching sequences and store in db, so for each insert it will fetch next sequence value.
