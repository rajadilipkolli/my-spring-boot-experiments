# spring-boot-togglz-sample

The toggle feature in a Spring Boot application is a mechanism that allows the developer to enable or disable certain features of the application without having to restart the entire application. This is useful for testing and debugging purposes, as it allows the developer to quickly enable or disable certain features without having to go through the time-consuming process of restarting the application. The toggle feature can be accessed through the application's configuration files, where the developer can specify which features should be enabled or disabled.

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
* Prometheus: http://localhost:9090/
* Grafana: http://localhost:3000/ (admin/admin)
* Kibana: http://localhost:5601/
