# GraphQL with WebFlux

[Watch the implementation video](https://www.youtube.com/watch?v=kVSYVhmvNCI&t=876s)

Implements a reactive GraphQL server using Spring WebFlux, enabling highly scalable endpoint queries.

## Key Points

- **Reactive Execution**: Non-blocking data fetchers.
- **Scalable**: Ideal for high-concurrency scenarios.
- **Performance**: Handles thousands of concurrent requests with minimal overhead.
- **Memory Efficient**: Reduces memory footprint through backpressure handling.

---

The spring-boot-starter-graphql is a starter dependency for Spring Boot applications that allows them to integrate with GraphQL APIs. It provides a set of tools and libraries that enable developers to easily build GraphQL-based applications and expose them through a GraphQL endpoint. The starter includes support for GraphQL queries, mutations, subscriptions, and schema definitions, as well as integration with Spring Boot's autoconfiguration and dependency injection features. 

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
* GraphIQL: http://localhost:8080/graphiql

### About Graph QL
GraphQL provides three main concepts:
1. **Queries**: Read data from the server
2. **Mutations**: Update data on the server
3. **Subscriptions**: Read data over a period of time (e.g., stock market updates, inflight recorder)

### How to fetch data using URL

- There are two types of annotations that can be used expose API
    * `@SchemaMapping(typeName = "Query", field = "customers")` , Here typeName should be matching the schema declared in schema.graphqls and filed should match the definition
    * `@QueryMapping` , Short hand for `@SchemaMapping` where field if not specified will be obtained from methodname and it should be declared in schema.graphqls
   
Sample data 

```
 {
    customers {
    id
    name
    orders {
        id
     }
   }
  }
```
   
or 

```
{
    customers {
     id
    }
}
```

Fetching data based on name

```
{
    customersByName(name: "kolli") {
        id
        name
    }
}
```
