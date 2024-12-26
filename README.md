[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/rajadilipkolli/my-spring-boot-experiments)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# Spring Boot Integration Experiments

This repository contains a collection of sample projects and experiments showcasing various Spring Boot integrations and features. The goal is to help you explore new possibilities, demonstrate best practices, and learn advanced concepts in a unified space.

---

<!-- TOC -->
* [Spring Boot Integration Experiments](#spring-boot-integration-experiments)
  * [Overview](#overview)
  * [Projects](#projects)
  * [Tech Stack](#tech-stack)
  * [Useful Docker Commands](#useful-docker-commands)
  * [Useful git Commands](#useful-git-commands)
<!-- TOC -->

---

## Overview

Each subdirectory in this repository demonstrates a focused aspect of Spring Boot development ranging from database integrations, caching, messaging, and multi-tenancy to advanced topics like Chaos Engineering and reactive stacks. Many of these samples are production ready, while some are still works-in-progress (WIP) intended for demonstration or experimentation.

**Key Features**:
1. **Wide Range of Integrations**: Explore different databases (MySQL, PostgreSQL, MongoDB, Oracle), caching with Redis, and advanced topics like multi-tenancy.
2. **Observability & Monitoring**: Many samples include Prometheus, Grafana, or Kibana. They demonstrate setting up and analyzing application performance in real time.
3. **Scalability & Resilience Patterns**: Investigate chaos monkey for injecting controlled failures, or look at multi-database solutions for horizontal scaling.

---
## Projects

Below is a quick lookup table summarizing each sub-project. For more details, check their individual README.md files.


| Name                                                                                                                    | Description 		                                                                                                                                                                                                                                                                                                                    | Status 		 |
|-------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| [Spring Batch Implementation](./batch-boot-jpa-sample)                                                                  | Demonstrates how to use Spring Batch 5 with a straightforward configuration, generate batch tables via Liquibase, and produce an SBOM (Software Bill of Materials).                                                                                                                                                               | Completed |
| [Archunit Implementation](./boot-api-archunit-sample)                                                                   | Shows how to enforce architectural constraints in a Spring Boot project using ArchUnit rules.                                                                                                                                                                                                                                     | Completed |
| [Chaos Engineering Principles](./boot-chaos-monkey)                                                                     | Covers applying Chaos Engineering with Spring Boot, along with performance testing via Gatling to highlight the impact of chaos experiments.                                                                                                                                                                                      | WIP       |
| [Grafana LGTM](./boot-grafana-lgtm)                                                                                     | Demonstrates an observability stack with Loki, Grafana, Tempo, and Mimir to handle metrics, traces, and logs in a Spring Boot context.                                                                                                                                                                                            | Completed |
| [Mongodb and Elasticsearch Reactive integration](./boot-mongodb-elasticsearch)                                          | Illustrates storing data in MongoDB, then retrieving it using reactive ElasticSearch for quick, geospatial, and reactive-based searches.                                                                                                                                                                                          | WIP       |
| [Opensearch Integration](./boot-opensearch-sample)                                                                      | Showcases saving data and performing swift geospatial searches in OpenSearch as an ElasticSearch alternative.                                                                                                                                                                                                                     | Completed |
| [Rabbit Mq Implementation](./boot-rabbitmq-thymeleaf)                                                                   | Demonstrates RabbitMQ producer acknowledgments, a Dead Letter Queue setup, and a Thymeleaf-powered UI.                                                                                                                                                                                                                            | Completed |
| [Rest API Documentation with examples](./boot-rest-docs-sample)                                                         | Highlights generating PDF documentation of RESTful APIs using Spring REST Docs.                                                                                                                                                                                                                                                   | Completed |  
| [Implementation of Strategy Design Pattern](./boot-strategy-plugin)                                                     | Uses the Strategy Pattern within a Spring application, builds a native image with GraalVM, and provides a main class to launch the application natively.                                                                                                                                                                          | Completed |
| [Feature Toggles](./boot-togglz-sample)                                                                                 | Demonstrates toggling specific features on or off at runtime in a Spring Boot project.                                                                                                                                                                                                                                            | Completed |
| [Ultimate Redis Implementation](./boot-ultimate-redis)                                                                  | Explores multiple Redis usage patterns with varying time-to-live (TTL) settings, including querying data via Spring Data Redis, connecting to Redis-cluster.                                                                                                                                                                      | Completed |
| [Graph QL implementation using QueryDSL](./graphql/boot-graphql-querydsl)                                               | Illustrates integrating GraphQL with QueryDSL to interact with a database in a Spring application.                                                                                                                                                                                                                                | WIP       |
| [Graph QL implementation using webflux](./graphql/boot-graphql-webflux)                                                 | Showcases using GraphQL alongside the reactive WebFlux stack for asynchronous data retrieval.                                                                                                                                                                                                                                     | Completed |
| [Graph QL implementation using webmvc](./graphql/boot-graphql-webmvc)                                                   | Demonstrates applying GraphQL concepts with the traditional Spring MVC for synchronous data handling.                                                                                                                                                                                                                             | WIP       |
| [Custom SequenceNumber and LazyConnectionDataSourceProxy for db connection improvement](./jpa/boot-data-customsequence) | 1. Highlights creating custom sequence generators <br/> 2. Optimizing database connections with `LazyConnectionDataSourceProxy` <br/> 3. Observing SQL statements via `datasource-proxy`, and validating queries in integration tests using `SQLStatementCountValidator` <br/> 4. Dynamic input validation using ValidationGroups | Completed |
| [Hibernate Envers Implementation using spring data JPA](./jpa/boot-data-envers)                                         | Shows how to track entity revisions with Hibernate Envers and monitor system health, triggering alerts on high CPU usage or downtime.                                                                                                                                                                                             | Completed |
| [Connecting to multiple data sources](./jpa/boot-data-multipledatasources)                                              | Demonstrates configuring multiple SQL databases in a single Spring Boot application, handling connection pools, and running both Liquibase and Flyway migrations.                                                                                                                                                                 | Completed |
| [Hibernate 2nd Level Cache Using Redis](./jpa/boot-hibernate2ndlevelcache-sample)                                       | Explains configuring Hibernate’s second-level caching via Redis and testing using query counting, with a custom repository approach.                                                                                                                                                                                              | Completed |
| [Read Replica Postgres with connection optimization](./jpa/boot-read-replica-postgresql)                                | Illustrates writing data to a primary Postgres database, then reading from a replica using `LazyConnectionDataSourceProxy` for improved performance                                                                                                                                                                               | Completed |
| [KeySet pagination and dynamic search with Blaze](./jpa/keyset-pagination/blaze-persistence)                            | Implements keyset pagination using Blaze Persistence, adding dynamic query filters with specifications.                                                                                                                                                                                                                           | Completed |
| [KeySet pagination and dynamic search with sring data jpa](./jpa/keyset-pagination/boot-data-window-pagination)         | Implements keyset pagination using Spring Data JPA, enabling dynamic queries through JPA specifications.                                                                                                                                                                                                                          | Completed |
| [MultiTenancy with multipledatsources](./jpa/multitenancy/multidatasource-multitenancy)                                 | Provides examples of running multi-tenant applications under different strategies, each with its own data source.                                                                                                                                                                                                                 | Completed |
| [MultiTenancy DB Based](./jpa/multitenancy/multitenancy-db)                                                             | Demonstrates having each tenant use a separate database (while sharing the same schema design).                                                                                                                                                                                                                                   | Completed |
| [MultiTenancy Partition Based](./jpa/multitenancy/partition)                                                            | Shows how to share a single database and table across tenants using a partition-based approach. (i.e., Shared Database with Shared table)                                                                                                                                                                                         | Completed |
| [MultiTenancy Schema Based](./jpa/multitenancy/schema)                                                                  | Demonstrates isolating tenants by allocating a separate schema for each tenant within the same database (i.e., Shared Database with Separate Schema)                                                                                                                                                                              | Completed |
| [Reactive SQL With JOOQ](./r2dbc/boot-jooq-r2dbc-sample)                                                                | Explains performing reactive CRUD operations in Spring Boot using jOOQ.                                                                                                                                                                                                                                                           | Completed |
| [Reactive Cache with Redis](./r2dbc/boot-r2dbc-reactive-cache)                                                          | Demonstrates caching reactive database operations in Redis to boost performance.                                                                                                                                                                                                                                                  | Completed |
| [Reactive Application](./r2dbc/boot-r2dbc-sample)                                                                       | Illustrates an end-to-end reactive CRUD workflow in Spring Boot using R2DBC.                                                                                                                                                                                                                                                      | Completed |
| [BackgroundJobs and Scheduling using Jobrunr](./scheduler/boot-scheduler-jobrunr)                                       | Sets up background job scheduling with [Jobrunr](https://www.jobrunr.io/en/) for asynchronous task execution.                                                                                                                                                                                                                     | Completed |
| [Scheduling using Quartz](./scheduler/boot-scheduler-quartz)                                                            | Showcases the [Quartz Scheduler](https://www.quartz-scheduler.org/), providing a Thymeleaf UI to pause, resume, create, and delete scheduled tasks.                                                                                                                                                                               | Completed |
| [Scheduling using Database Distributed Locks with ShedLock](./scheduler/boot-scheduler-shedlock)                        | Demonstrates scheduling while ensuring only one active job running at a time via ShedLock.                                                                                                                                                                                                                                        | Completed |


For More info about this repository, Please visit [here](https://rajadilipkolli.github.io/my-spring-boot-experiments/)

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

Start postgres and pgadmin
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
