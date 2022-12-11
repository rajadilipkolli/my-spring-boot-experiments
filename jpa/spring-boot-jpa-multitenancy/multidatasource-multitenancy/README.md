# multidatasource-multitenancy

Multi-tenancy is a software architecture in which a single instance of a software application is used to serve multiple tenants. Each tenant is a separate entity that has its own unique data, configuration, and user base. This architecture allows for efficient resource utilization, as the software application only needs to be installed and maintained once, but can be used by multiple tenants.

Multidatabases is a similar concept, but applies to databases rather than software applications. In this architecture, multiple databases are used to store and manage data for different tenants. This allows for efficient resource utilization, as the same database server can be used to host multiple databases. It also allows for greater flexibility, as different tenants can have different database configurations and access controls.

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
* PgAdmin : http://localhost:5050
