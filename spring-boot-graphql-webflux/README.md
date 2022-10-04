# spring-boot-graph-ql [SourceCode](https://www.youtube.com/watch?v=kVSYVhmvNCI&t=876s)


### Run tests
`$ ./gradlew clean build`

### Run locally
`$ ./gradlew bootRun -Plocal`

### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui/index.html
* Actuator Endpoint: http://localhost:8080/actuator

### About Graph QL
It contains 3 concepts
* Queries - Read data from the Server
* Mutations - Update data on the Server
* Subscriptions - Read data over a period of time(Stock market updates, inflight recorder)

### How to fetch data using URL

- There are two types of annotations that can be used expose API
    * `@SchemaMapping(typeName = "Query", field = "customers")` , Here typeName should be matching the schema declared in schema.graphqls and filed should match the definition
    * `@QueryMapping` , Short hand for `@SchemaMapping` where field if not specified will be obtained from methodname and it should be declared in schema.graphqls

- http://localhost:8080/graphiql
   
Sample data 

 `{
    customers {
    id
    name
    orders {
        id
     }
   }
  }`
   
or 

`
{
    customers {
     id
    }
}
`

Fetching data based on name

`{
customersByName(name: "kolli") {
id
name
}
}
`

