# GraphQL with Spring MVC

Demonstrates a blocking GraphQL approach via Spring MVC for synchronous data fetching.

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
* GraphQlUI : http://localhost:8080/graphiql
* PgAdmin : http://localhost:5050
* Prometheus : http://localhost:9090
* Grafana : http://localhost:3000 (admin/admin)
