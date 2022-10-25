# spring-boot-choas-monkey

Adds Gatline performance tests to demonstrate the adding assualts will cause delay in response and choas monkey is working as expected 

### Run tests
`$ ./mvnw clean verify`

### Run Gatling 
`./mvnw gatling:test`

### Run locally
```
$ docker-compose -f docker/docker-compose.yml up -d
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
$ ./mvnw gatling:test
```

By Default choas Monkey is enabled, lets disabled and run the tests again. It should improve the response times

```
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=local -Dchaos.monkey.enabled=false
$ ./mvnw gatling:test
```


### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator

### Technologies used
* Spring Boot
* Gatling (Performance Tests)
* Choas Monkey (Choas Engineering Principles)