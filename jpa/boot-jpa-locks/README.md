# boot-jpa-locks

In database management, transactions are a sequence of operations performed as a single logical unit of work. A transaction must be handled in a way that is both atomic (all-or-nothing) and isolated (independent of other transactions).

**Isolation** is one of the ACID (Atomicity, Consistency, Isolation, Durability) properties that deal with concurrent transactions. It ensures that concurrently executing transactions result in a system state that would be obtained if transactions were executed serially, i.e., one after the other.

**Propagation** is a term specific to Spring transactions, and it determines what happens if a transactional method is executed when a transaction context already exists. There are several propagation behaviors available, but the default one is PROPAGATION_REQUIRED, which means that the same transaction will be used if there is an already opened transaction in the current bean method execution context, or a new one will be created if there isn't.

Here's a brief overview of other propagation modes:

1. **PROPAGATION_REQUIRES_NEW**: Always create a new transaction. If one already exists, suspend it.
2. **PROPAGATION_SUPPORTS**: Use the current transaction if one exists. If not, execute non-transactionally.
3. **PROPAGATION_NESTED**: Execute within a nested transaction if one exists or create a new one otherwise.

The isolation and propagation levels you should use depend on your specific needs. In general:

* If you need to ensure that your data remains consistent and you want to prevent dirty reads, non-repeatable reads, and phantom reads, you should use a higher isolation level like **SERIALIZABLE**. However, this comes at the cost of performance, as it locks the data for the duration of the transaction.
* If performance is a priority and you can tolerate some inconsistencies, you should use a lower isolation level like **READ_UNCOMMITTED**.
* Regarding propagation, if you need to execute multiple operations as a single unit of work, use **PROPAGATION_REQUIRED**. If you need to isolate some operations from your current transaction, use **PROPAGATION_REQUIRES_NEW**. If you're going to execute some operations outside a transaction, use PROPAGATION_SUPPORTS.

Remember, choosing the right isolation and propagation level is a trade-off between data consistency and performance.


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
