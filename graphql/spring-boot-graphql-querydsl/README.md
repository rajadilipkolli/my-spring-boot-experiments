# GraphQL with QueryDSL

GraphQL is a query language for APIs that allows for more flexible and efficient data querying and manipulation. QueryDSL is a Java-based library that allows for the creation of type-safe queries in a variety of query languages, including GraphQL.

Together, GraphQL and QueryDSL can provide developers with a powerful toolset for building and managing complex data-driven applications. QueryDSL allows for the creation of type-safe queries in GraphQL, providing a more robust and efficient way to query data from APIs. Additionally, QueryDSL's support for multiple query languages means that developers can easily switch between different query languages without having to rewrite their code.


### Run tests

`./gradlew clean build`

### Run locally

```shell
docker-compose -f docker/docker-compose.yml up -d
./gradlew bootRun -Plocal
```

### Useful Links

* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator

