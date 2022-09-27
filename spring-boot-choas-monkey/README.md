# spring-boot-choas-monkey


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
* Prometheus: http://localhost:9090/
* Grafana: http://localhost:3000/ (admin/admin)
* Kibana: http://localhost:5601/

### Notes
Based on choas engineering principles
 - Choas can be introduced at Controller, Service, Repositort and RestTemplate or WebClient Level 
 - Choas can be of two types Assults and Watchers
 - To Enable Choas using starter need to activate the choas-monkey profile which comes OOTB


### Reference
 - https://codecentric.github.io/chaos-monkey-spring-boot/