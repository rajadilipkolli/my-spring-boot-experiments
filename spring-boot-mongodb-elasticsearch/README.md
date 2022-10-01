# spring-boot-mongodb-elasticsearch

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
* Kibana: http://localhost:5601/app/kibana#/dev_tools/console?_g=()
* Elasticsearch : http://localhost:9200/
* MongoExpress : http://localhost:8081


## Elastic Search Notes

### UseFull ElasticSearch Commands
- Count http://localhost:9200/restaurant/_count
- Search http://localhost:9200/restaurant/_search
- Mapping http://localhost:9200/restaurant/_mapping


### Reference
- https://medium.com/geekculture/elastic-search-queries-hands-on-examples-fe5b2bc10c0e
