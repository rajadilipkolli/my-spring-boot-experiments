## Connects to multiple datasources

This example demostrates how to connect to multiple datasources in same spring boot project

## Databases and Migration Tools used
 - [Postgresql](./src/main/java/com/example/multipledatasources/model/cardholder)
 - [MySQL](./src/main/java/com/example/multipledatasources/model/member)
 - Liquibase - DataBase Migration Tool for Postgres Database
 - Flyway - DataBase Migration Tool for MySQL Database
 - Swagger - http://localhost:8080/swagger-ui.html

### Notes

    Postgres Database supports Sequences where as MySQL doesn't supports it hence we need to use Identity Generation value Strategy

 - Flow as soon as application is started first liquibase and flyway database migrations are executed
 - As soon as application is ready bootstrapping of data is done using `@EventListener(ApplicationReadyEvent.class)`
 - Data from both databases are retrieved in API and merged as response
 - Json serialization and deSerialization is done using `@JsonTest`
 - When we set HikariDataSource manually we need to set `app.datasource._______.hikari.autoCommit=false`

### Upgrade Notes to Spring Boot 3.x

 - Hibernate 6 By default enables pool-lo sequencing strategy with `table_seq` name instead of hibernate_seq with allocation size 50
 - javax.persistance is moved to jakarta.persistance
 - added validation starter
