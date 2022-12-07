# spring-boot-hibernate2ndlevelcache-sample

The Hibernate second level cache is a cache that is used to store the data that has been retrieved from the database. This cache is used to improve the performance of the application by reducing the number of trips to the database and providing quick access to frequently used data. The second level cache is typically implemented at the session factory level and is shared across all sessions within the factory. It is also configurable and can be enabled or disabled as needed. It is separate from the first level cache, which is associated with a session and only stores objects for the duration of that session. The second level cache is shared across sessions and can be configured to use various cache providers, such as Ehcache or Infinispan.

### Run tests

`$ ./mvnw clean verify`

### Run locally

```
$ docker-compose -f docker/docker-compose.yml up -d
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Useful Links

* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
* Prometheus: http://localhost:9090/
* Grafana: http://localhost:3000/ (admin/admin)
* Kibana: http://localhost:5601/

### Notes

* We need to explicitly set the querycacheHint to customerqueries for enabling 2nd level cache
* This is enabled only for SessionFactory(i.e as soon as application is closed it will be deleted)
