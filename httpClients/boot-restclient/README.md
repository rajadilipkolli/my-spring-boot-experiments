# Spring 6 RestClient

Introduces the new RestClient in Spring 6.2 as an alternative to WebClient or RestTemplate for synchronous usage (depending on the environment).

## Why RestClient?

- **Unified API**: Replaces older RestTemplate approach, offering a modern, extensible framework.

---

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
