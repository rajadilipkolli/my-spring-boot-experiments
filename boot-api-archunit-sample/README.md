# boot-api-archunit-sample

 ![](../images/ArchUnit-Logo.png) 

 ArchUnit is a Java-based library that allows developers to create unit tests for the architecture of their code. It provides a set of rules and assertions that can be used to validate the architecture of the code, ensuring that it follows best practices and meets certain architectural requirements. This allows developers to catch potential issues early on in the development process and improve the overall quality and maintainability of the code.

 ArchUnit Implementation in a Spring Boot Project, read [more](https://www.archunit.org/motivation) for understanding concept

Shamelessly copied from [api-example-archunit](https://github.com/andres-sacco/api-example-archunit)

The idea behind this API is to show some of all possibles rules that you can validate in one particular project. For more information about this library and how you can use it check his [page](https://sacco-andres.medium.com/)

### Format code

```shell
$ ./mvnw spotless:apply
```

### Run tests

```shell
$ ./mvnw clean verify
```

### Run locally

```shell
$ docker-compose -f docker/docker-compose.yml up -d
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
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

### Lessons
* Migrated from flyway to liquibase bacause flyway is stopping upgrade to latest version of postgres

Here are some of technologies that I used to develop this example:
* Spring Boot - 3.3.0
* Archunit - 1.2.0
* Junit5