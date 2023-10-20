# boot-rest-template


## Spring RestTemplate Overview

`RestTemplate` is a class in the Spring Framework that is used to perform HTTP requests to RESTful web services. It is a high-level, easy-to-use interface that abstracts away the complexity of making requests and handling responses.

RestTemplate provides several methods for making HTTP requests, including getForObject, postForObject, put, and delete. These methods can be used to perform a variety of HTTP requests, such as GET, POST, PUT, DELETE, etc., and can accept arguments such as the URL of the web service, the request body (for POST and PUT requests), and any headers or query parameters that need to be included in the request.

RestTemplate also provides convenience methods for handling responses, such as getForEntity, which returns a ResponseEntity object that can be used to access the response body, headers, and status code.

RestTemplate is a useful tool for interacting with RESTful web services in the Spring Framework, and is often used in Spring-based applications to consume web services.


### Run tests
`$ ./mvnw clean verify`

### Run locally
```shell
$ docker-compose -f docker/docker-compose.yml up -d
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```


### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
