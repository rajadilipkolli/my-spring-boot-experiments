# GraphQL with QueryDSL

Combines GraphQL’s flexible schema definition and QueryDSL’s type-safe query generation in a Spring Boot environment.

## Why Use QueryDSL with GraphQL?

- **Rich Filtering**: Type-safe queries that can evolve with your schema.
- **Reduced Boilerplate**: QueryDSL auto-generates domain-based classes.

---

GraphQL is a query language for APIs that allows for more flexible and efficient data querying and manipulation. QueryDSL is a Java-based library that allows for the creation of type-safe queries in a variety of query languages, including GraphQL.

Together, GraphQL and QueryDSL can provide developers with a powerful toolset for building and managing complex data-driven applications. QueryDSL allows for the creation of type-safe queries in GraphQL, providing a more robust and efficient way to query data from APIs. Additionally, QueryDSL's support for multiple query languages means that developers can easily switch between different query languages without having to rewrite their code.

### Format code

```shell
./mvnw spotless:apply
```

### Run tests

```shell
./mvnw clean verify
```

### Run locally

```shell
docker-compose -f docker/docker-compose.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
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
