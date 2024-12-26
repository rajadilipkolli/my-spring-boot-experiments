# Spring Boot Chaos Monkey

[Choas Monkey](https://netflix.github.io/chaosmonkey/)

This module explores Chaos Engineering principles by intentionally injecting failures into a Spring Boot application to test its resilience. It demonstrates how to set up and integrate `chaos-monkey-spring-boot` for your microservices, as well as performance testing with Gatling to measure any degradation under chaos scenarios.


![](../images/chaos-monkey.png)

---

## Key Features

1. **Chaos Injection**: Simulate random latencies, exceptions, or resources unavailability.
2. **Performance Testing** (Gatling): Benchmark throughput and latency under chaotic conditions.
3. **Monitoring**: Validate that chaos is functioning by observing logs, metrics, or custom dashboards.

---

 
### Run tests

```shell
./mvnw clean verify
```

### Run Gatling

Bring the application up and then start Gatling

```shell
./mvnw gatling:test
```

### Run locally

```shell
docker-compose -f docker/docker-compose.yml up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.arguments="--chaos.monkey.enabled=true"
./mvnw gatling:test
```

**Verification**:

* Check logs to confirm chaos injection events.
* Use Gatling or other load testing tools to verify how the application behaves under stress.

By default, Chaos Monkey is enabled. Let's disable it and run the tests again. It should improve the response times.

```shell
./mvnw spring-boot:run -Dspring-boot.run.profiles=local -Dspring-boot.run.arguments="--chaos.monkey.enabled=false"
./mvnw gatling:test
```

Results of gatling are stored in target folder for each run

### Useful Links

* Swagger UI: http://localhost:8080/swagger-ui.html
* Actuator Endpoint: http://localhost:8080/actuator
* PGAdmin : http://localhost:5050
* Obervability metrics : http://localhost:8080/actuator/metrics

### Technologies used

* Spring Boot
* Gatling (Performance Tests)
* Chaos Monkey (Chaos Engineering Principles)

#### Sample Test Results

We can observe that mean requests/sec is more when Chaos Monkey is enabled.

### With Chaos Monkey Enabled

\================================================================================ ---- Global Information --------------------------------------------------------

> request count 6000 (OK=6000 KO=0 ) min response time 1 (OK=1 KO=- ) max response time 2635 (OK=2635 KO=- ) mean response time 145 (OK=145 KO=- ) std deviation 337 (OK=337 KO=- ) response time 50th percentile 15 (OK=15 KO=- ) response time 75th percentile 98 (OK=98 KO=- ) response time 95th percentile 773 (OK=773 KO=- ) response time 99th percentile 1952 (OK=1952 KO=- ) mean requests/sec 193.548 (OK=193.548 KO=- ) ---- Response Time Distribution ------------------------------------------------ t < 800 ms 5733 ( 96%) 800 ms <= t < 1200 ms 131 ( 2%) t ≥ 1200 ms 136 ( 2%) failed 0 ( 0%) ================================================================================

**With Chaos Monkey disabled**

\================================================================================ ---- Global Information --------------------------------------------------------

> request count 6000 (OK=6000 KO=0 ) min response time 4 (OK=4 KO=- ) max response time 13608 (OK=13608 KO=- ) mean response time 6289 (OK=6289 KO=- ) std deviation 2579 (OK=2579 KO=- ) response time 50th percentile 6396 (OK=6396 KO=- ) response time 75th percentile 8292 (OK=8292 KO=- ) response time 95th percentile 10342 (OK=10342 KO=- ) response time 99th percentile 11604 (OK=11604 KO=- ) mean requests/sec 176.471 (OK=176.471 KO=- ) ---- Response Time Distribution ------------------------------------------------ t < 800 ms 65 ( 1%) 800 ms <= t < 1200 ms 54 ( 1%) t ≥ 1200 ms 5881 ( 98%) failed 0 ( 0%) ================================================================================
