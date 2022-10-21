# spring-boot-data-envers-sample


### Run tests
`$ ./gradlew clean build`

### Run locally
```
$ docker-compose -f docker/docker-compose.yml up -d
$ ./gradlew bootRun -Plocal
```

### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator

### Update to Spring Boot 3
* Removed problem-spring-web-starter and replaced with Spring OOTB `spring.mvc.problemdetails.enabled=true`
* Migrated from javax namespace to jakarta namespace
* By Default hibernate_sequence is not used instead tablename_seq is used while mapping sequence name and `pooled-lo` generation strategy is used OOTB
