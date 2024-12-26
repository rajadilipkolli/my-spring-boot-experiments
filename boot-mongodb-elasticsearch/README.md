# MongoDB and Elasticsearch Reactive Integration

Integrates a reactive MongoDB backend with a reactive Elasticsearch instance for enhanced real-time searching capabilities, including geospatial queries.

---

## Highlights

- **Reactive Streams Approach**: Uses Spring Data’s reactive driver for both MongoDB and Elasticsearch.
- **Advanced Queries**: Demonstrates geospatial searches, text-indexed searches, and filtering in near real-time.
- **Scalable Architecture**: Docker-compose-based environment for local testing.

---

### Run tests

```shell
./mvnw clean verify
```

### Run locally
```shell
docker-compose -f docker/docker-compose.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Useful Links
* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
* Prometheus: http://localhost:9090/
* Grafana: http://localhost:3000/ (admin/admin)
* Kibana: http://localhost:5601/app/kibana#/dev_tools/console?_g=()
* Elasticsearch : http://localhost:9200/
* MongoExpress : http://localhost:8081

---

## Mongodb Notes

Transactions with `ReactiveMongoTransactionManager`

```
@Configuration
public class DataStoreConfiguration extends AbstractReactiveMongoConfiguration {

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Bean
    ReactiveMongoTransactionManager transactionManager(ReactiveMongoDatabaseFactory factory) {
        return new ReactiveMongoTransactionManager(factory);
    }

    @Override
    protected String getDatabaseName() {
        return this.databaseName;
    }
}
```
---

## Elasticsearch Notes

### UseFull ElasticSearch Commands
- Count http://localhost:9200/restaurant/_count
- Search http://localhost:9200/restaurant/_search
- Mapping http://localhost:9200/restaurant/_mapping


### Reference
- https://medium.com/geekculture/elastic-search-queries-hands-on-examples-fe5b2bc10c0e

### Exceptions & Resolutions
 * When using reactive elasticeSearch is we get below issue like raiseLimitException then solution should be to raise the memory using property

``` 
2022-10-01 21:24:14.156 ERROR 34465 --- [or-http-epoll-2] a.w.r.e.AbstractErrorWebExceptionHandler : [8006d397-1]  500 Server Error for HTTP GET "/restaurant?limit=1000&offset=1"

org.springframework.core.io.buffer.DataBufferLimitException: Exceeded limit on max bytes to buffer : 262144
        at org.springframework.core.io.buffer.LimitedDataBufferList.raiseLimitException(LimitedDataBufferList.java:99)
```


To fix this set `spring.elasticsearch.webclient.max-in-memory-size=-1` for unlimited memory
