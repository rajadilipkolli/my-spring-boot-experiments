# boot-hibernate2ndlevelcache-sample

The Hibernate second level cache is a cache that is used to store the data that has been retrieved from the database. This cache is used to improve the performance of the application by reducing the number of trips to the database and providing quick access to frequently used data. The second level cache is typically implemented at the session factory level and is shared across all sessions within the factory. It is also configurable and can be enabled or disabled as needed. It is separate from the first level cache, which is associated with a session and only stores objects for the duration of that session. The second level cache is shared across sessions and can be configured to use various cache providers, such as Ehcache or Infinispan.

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


### Notes

* We need to explicitly set the querycacheHint to customerqueries for enabling 2nd level cache
* This is enabled only for SessionFactory(i.e as soon as application is closed it will be deleted)

### **Caching Collections (One-Many & Many-Many Relations)**


Collection caching allows you to cache entire collections of associated entities. These collections can be part of your domain model, such as one-to-many or many-to-many relationships between entities.

Collection caching is valuable when dealing with associations between entities that are frequently loaded and where caching can lead to significant performance gains. When you enable collection caching, Hibernate caches entire collections, such as lists or sets, associated with an entity.

When Hibernate caches a collection, it doesn’t cache the entire collection of entities but rather caches the IDs of the entities contained in the collection.

* Caching only the IDs reduces memory usage compared to caching the entire collection of entities.
* When a collection is updated, only the relevant IDs need to be invalidated in the cache, rather than the entire collection. This minimizes cache invalidation overhead.
