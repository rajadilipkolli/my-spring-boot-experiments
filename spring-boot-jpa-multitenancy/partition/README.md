# Partitioned (Discriminator) Data – the data for each tenant is partitioned by a discriminator value

## Note this is supported only from Hibernate 6.x and Spring boot 3.x, each discriminator is annotated with `@Tenant` which will be added to the where clause automatically using TenantIdentifier Resolver

### Run tests
`$ ./mvnw clean verify`

### Run locally
```
$ docker-compose -f docker/docker-compose.yml up -d
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```


### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
