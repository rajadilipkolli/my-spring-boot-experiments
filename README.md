[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# Spring Boot Integration Experiments

This repository contains a collection of sample projects and experiments showcasing various Spring Boot integrations and features. The goal is to help you explore new possibilities, demonstrate best practices, and provide ready-to-use examples.

---

## Table of Contents
- [Overview](#overview)
- [Projects](#projects)
- [Tech Stack](#tech-stack)
- [Useful Docker Commands](#useful-docker-commands)
- [Useful git Commands](#useful-git-commands)

---

## Overview

Each subdirectory in this repository demonstrates a focused aspect of Spring Boot development ranging from database integrations, caching, messaging, and multi-tenancy to advanced topics like Chaos Engineering and observability.

**Key Features**:
1. **Wide Range of Integrations**: Explore different databases (MySQL, PostgreSQL, MongoDB, Oracle), caching with Redis, and advanced topics like multi-tenancy.
2. **Observability & Monitoring**: Many samples include Prometheus, Grafana, or Kibana. They demonstrate setting up and analyzing application performance in real-time.
3. **Scalability & Resilience Patterns**: Investigate Chaos Monkey for injecting controlled failures, or look at multi-database solutions for horizontal scaling.

---

## Projects

Below is a quick lookup table summarizing each sub-project. For more details, check their individual README.md files.

| Name                                                                                                             | Description 		                                                                                              |
|------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| [Spring Batch Implementation](./batch-boot-jpa-sample)                                                           | Demonstrates how to use Spring Batch 5 with a straightforward configuration.                                |
| [Archunit Implementation](./boot-api-archunit-sample)                                                            | Shows how to enforce architectural constraints in a Spring Boot project using ArchUnit.                     |
| [Chaos Engineering Principles](./boot-chaos-monkey)                                                              | Covers applying Chaos Engineering with Spring Boot, along with performance tests.                           |
| [Grafana LGTM](./boot-grafana-lgtm)                                                                              | Demonstrates an observability stack with Loki, Grafana, Tempo, and Mimir.                                   |
| [HTTP Proxy Implementation](./httpClients/boot-http-proxy)                                                       | Implements HTTP proxy functionality for routing and managing HTTP requests.                                 |
| [JMH Benchmark](./jmh-benchmark)                                                                                 | Provides microbenchmarking capabilities using JMH for measuring code performance.                           |
| [Mongodb and Elasticsearch Reactive integration](./boot-mongodb-elasticsearch)                                   | Illustrates storing data in MongoDB, then retrieving it using reactive Elasticsearch.                       |
| [OpenAPI Implementation](./open-api-spring-boot)                                                                 | Demonstrates OpenAPI specification generation and documentation using Swagger.                              |
| [Opensearch Integration](./boot-opensearch-sample)                                                               | Showcases saving data and performing swift geospatial searches in OpenSearch.                               |
| [Rabbit Mq Implementation](./boot-rabbitmq-thymeleaf)                                                            | Demonstrates RabbitMQ producer acknowledgments, a Dead Letter Queue setup, and more.                        |
| [Rest API Documentation with examples](./boot-rest-docs-sample)                                                  | Highlights generating PDF documentation of RESTful APIs using Spring REST Docs.                             |
| [RestClient Implementation](./httpClients/boot-restclient)                                                       | Introduces Spring 6's RestClient as a modern alternative to WebClient/RestTemplate.                         |
| [RestTemplate Implementation](./httpClients/boot-rest-template)                                                  | Shows how to use Spring's RestTemplate for making HTTP requests to RESTful services.                        |
| [Implementation of Strategy Design Pattern](./boot-strategy-plugin)                                              | Uses the Strategy Pattern within a Spring application, builds a native image.                               |
| [Feature Toggles](./boot-togglz-sample)                                                                          | Demonstrates toggling specific features on or off at runtime in a Spring Boot application.                  |
| [Ultimate Redis Implementation](./boot-ultimate-redis)                                                           | Explores multiple Redis usage patterns with varying time-to-live (TTL) settings.                            |
| [WebClient MVC Implementation](./httpClients/boot-web-client-mvc)                                                | Demonstrates using WebClient with Spring MVC for HTTP operations.                                           |
| [WebClient WebFlux Implementation](./httpClients/boot-web-client-webflux)                                        | Shows WebClient usage with WebFlux for reactive HTTP operations.                                            |
| [Graph QL implementation using QueryDSL](./graphql/boot-graphql-querydsl)                                        | Illustrates integrating GraphQL with QueryDSL to interact with a database.                                  |
| [Graph QL implementation using webflux](./graphql/boot-graphql-webflux)                                          | Showcases using GraphQL alongside the reactive WebFlux stack for asynchronous processing.                   |
| [Graph QL implementation using webmvc](./graphql/boot-graphql-webmvc)                                            | Demonstrates applying GraphQL concepts with the traditional Spring MVC for synchronous processing.          |
| [Custom SequenceNumber and LazyConnectionDataSourceProxy](./jpa/boot-data-customsequence)                        | Highlights creating custom sequence generators and optimizing database connections.                         |
| [Hibernate Envers Implementation using spring data JPA](./jpa/boot-data-envers)                                  | Shows how to track entity revisions with Hibernate Envers and monitor system changes.                       |
| [Connecting to multiple data sources](./jpa/boot-data-multipledatasources)                                       | Demonstrates configuring multiple SQL databases in a single Spring Boot application.                        |
| [Hibernate 2nd Level Cache Using Redis](./jpa/boot-hibernate2ndlevelcache-sample)                                | Explains configuring Hibernate's second-level caching via Redis and testing it.                             |
| [JNDI Sample](./jpa/boot-jndi-sample)                                                                            | Demonstrates JNDI usage with embedded Tomcat and Hikari DataSource.                                         |
| [JPA with JOOQ](./jpa/boot-jpa-jooq-sample)                                                                      | Shows how to use both JPA and JOOQ in the same application for database operations.                         |
| [JPA Locks](./jpa/boot-jpa-locks)                                                                                | Demonstrates database locking mechanisms and transaction isolation levels.                                  |
| [Read Replica Postgres with connection optimization](./jpa/boot-read-replica-postgresql)                         | Illustrates writing data to a primary Postgres database, then reading from a replica.                       |
| [KeySet pagination and dynamic search with Blaze](./jpa/keyset-pagination/blaze-persistence)                     | Implements keyset pagination using Blaze Persistence, adding dynamic query features.                        |
| [KeySet pagination and dynamic search with spring data jpa](./jpa/keyset-pagination/boot-data-window-pagination) | Implements keyset pagination using Spring Data JPA, enabling dynamic queries.                               |
| [MultiTenancy with multipledatsources](./jpa/multitenancy/multidatasource-multitenancy)                          | Provides examples of running multi-tenant applications under different strategies.                          |
| [MultiTenancy DB Based](./jpa/multitenancy/multitenancy-db)                                                      | Demonstrates having each tenant use a separate database (while sharing the same codebase).                  |
| [MultiTenancy Partition Based](./jpa/multitenancy/partition)                                                     | Shows how to share a single database and table across tenants using a partitioning strategy.                |
| [MultiTenancy Schema Based](./jpa/multitenancy/schema)                                                           | Demonstrates isolating tenants by allocating a separate schema for each tenant.                             |
| [Reactive SQL With JOOQ](./r2dbc/boot-jooq-r2dbc-sample)                                                         | Explains performing reactive CRUD operations in Spring Boot using jOOQ.                                     |
| [PostgreSQL JSON and ENUM column support using reactive](./r2dbc/boot-r2dbc-json-column)                         | Demonstrates PostgreSQL JSON and ENUM column support in a reactive application.                             |
| [PostgreSQL Notify/Listen](./r2dbc/boot-r2dbc-notify-listen)                                                     | Shows how to implement PostgreSQL's NOTIFY/LISTEN feature using R2DBC.                                      |
| [Reactive Cache with Redis](./r2dbc/boot-r2dbc-reactive-cache)                                                   | Demonstrates caching reactive database operations in Redis to boost performance.                            |
| [Reactive Application](./r2dbc/boot-r2dbc-sample)                                                                | Illustrates an end-to-end reactive CRUD workflow in Spring Boot using R2DBC.                                |
| [BackgroundJobs and Scheduling using Jobrunr](./scheduler/boot-scheduler-jobrunr)                                | Sets up background job scheduling with [Jobrunr](https://www.jobrunr.io/en/).                               |
| [Scheduling using Quartz](./scheduler/boot-scheduler-quartz)                                                     | Showcases the [Quartz Scheduler](https://www.quartz-scheduler.org/), providing job scheduling capabilities. |
| [Scheduling using Database Distributed Locks with ShedLock](./scheduler/boot-scheduler-shedlock)                 | Demonstrates scheduling while ensuring only one active job running at a time using ShedLock.                |

---

## Tech Stack

This repository leverages a broad stack of technologies, including:

- **Java** / **JavaScript** / **SQL** for core logic & data.
- **Spring Framework** (Boot, Data, Security, etc.) as the key application framework.
- **Project Reactor** for reactive data flows and concurrency.
- **QueryDSL**, **Liquibase**, **Flyway**, **JOOQ** for advanced database interactions and migrations.
- **Docker**, **Docker Compose** for containerization and multi-service orchestration.
- **RabbitMQ**, **Redis**, **Elasticsearch**, **OpenSearch**, **MongoDB** for messaging, caching, and search.
- **Gradle** / **Maven** for builds and dependency management.
- **GitHub Actions**, **CircleCI**, **Jenkins** for CI/CD pipelines.

For in-depth version references, visit [techstack.md](/techstack.md) or see individual project READMEs.

---

## Useful Docker Commands

Start Postgres and pgAdmin
 ```shell
 docker compose up postgres pgadmin4
 ```
Clean up everything using
 ```shell
 docker system prune -a -f --volumes
 ```
Claim unused volumes
 ```shell
 docker volume prune
 ```
Running container
 ```shell
 docker container ls
 ```

---

## Useful git Commands

How to overwrite local changes with git pull

Stash local changes:
 ```shell
 git stash
 ```
Pull changes from remote:
 ```shell
 git pull
 ```
How to revert the changes that are pushed to remove
```shell
git revert $hash
```
