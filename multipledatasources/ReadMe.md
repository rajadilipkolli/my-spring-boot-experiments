## Connects to multiple datasources

This example demostrates how to connect to multiple datasources in same spring boot project

## Databases and Migration Tools used
 - [Postgresql](./src/main/java/com/example/multipledatasources/model/cardholder)
 - [MySQL](./src/main/java/com/example/multipledatasources/model/member)
 - Liquibase - DataBase Migration Tool for Postgres Database
 - Flyway - DataBase Migration Tool for MySQL Database

### Notes

    Postgres Database supports Sequences where as MySQL doesn't supports it hence we need to use Identity Generation value Strategy

 - Flow as soon as application is started first liquibase and flyway database migrations are executed
 - As soon as application is ready bootstrapping of data is done using `@EventListener(ApplicationReadyEvent.class)`
 - Data from both databases are retrieved in API and merged as response
 - Json serialization and deSerialization is done using `@JsonTest`