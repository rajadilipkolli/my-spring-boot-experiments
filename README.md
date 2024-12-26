[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/rajadilipkolli/my-spring-boot-experiments)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)


<!-- TOC -->
* [spring boot integration experiments](#spring-boot-integration-experiments)
    * [Tech Stack](#tech-stack)
    * [Useful Docker Commands](#useful-docker-commands)
    * [Useful git Commands](#useful-git-commands)
<!-- TOC -->

# spring boot integration experiments

The following table list all sample codes related to the spring boot integrations.

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

### Tech Stack
Repo is built on the following main stack:

- <img width='25' height='25' src='https://img.stackshare.io/service/995/K85ZWV2F.png' alt='Java'/> [Java](https://www.java.com) – Languages
- <img width='25' height='25' src='https://img.stackshare.io/service/1209/javascript.jpeg' alt='JavaScript'/> [JavaScript](https://developer.mozilla.org/en-US/docs/Web/JavaScript) – Languages
- <img width='25' height='25' src='https://img.stackshare.io/service/2271/default_068d33483bba6b81ee13fbd4dc7aab9780896a54.png' alt='SQL'/> [SQL](https://en.wikipedia.org/wiki/SQL) – Languages
- <img width='25' height='25' src='https://img.stackshare.io/service/1011/n1JRsFeB_400x400.png' alt='Node.js'/> [Node.js](http://nodejs.org/) – Frameworks (Full Stack)
- <img width='25' height='25' src='https://img.stackshare.io/service/5807/default_cbd8ab670309059d7e315252d307d409aa40d793.png' alt='Project Reactor'/> [Project Reactor](https://projectreactor.io/) – Java Tools
- <img width='25' height='25' src='https://img.stackshare.io/service/3881/da8da0c0e6dfbfc596f787ade49381a6_400x400.png' alt='QueryDSL'/> [QueryDSL](http://www.querydsl.com/) – Java Tools
- <img width='25' height='25' src='https://img.stackshare.io/service/2006/spring-framework-project-logo.png' alt='Spring Framework'/> [Spring Framework](https://spring.io/projects/spring-framework) – Frameworks (Full Stack)
- <img width='25' height='25' src='https://img.stackshare.io/service/1021/lxEKmMnB_400x400.jpg' alt='jQuery'/> [jQuery](http://jquery.com/) – Javascript UI Libraries
- <img width='25' height='25' src='https://img.stackshare.io/service/21275/default_078eb0ae2b56280a937ed073a3ba4332291f9ba8.png' alt='Cloud DB for Mysql'/> [Cloud DB for Mysql](https://www.ncloud.com/product/database/cloudDbMysql) – SQL Database as a Service
- <img width='25' height='25' src='https://img.stackshare.io/service/3105/h2-logo_square_400x400.png' alt='H2 Database'/> [H2 Database](http://www.h2database.com/) – Databases
- <img width='25' height='25' src='https://img.stackshare.io/service/1398/y1As8_s5_400x400.jpg' alt='Liquibase'/> [Liquibase](https://www.liquibase.com) – Database Tools
- <img width='25' height='25' src='https://img.stackshare.io/service/1615/mariadb-logo-400x400.png' alt='MariaDB'/> [MariaDB](https://mariadb.com/) – Databases
- <img width='25' height='25' src='https://img.stackshare.io/service/1027/sql_server.png' alt='Microsoft SQL Server'/> [Microsoft SQL Server](http://microsoft.com/sqlserver) – Databases
- <img width='25' height='25' src='https://img.stackshare.io/service/1030/leaf-360x360.png' alt='MongoDB'/> [MongoDB](http://www.mongodb.com/) – Databases
- <img width='25' height='25' src='https://img.stackshare.io/service/1025/logo-mysql-170x170.png' alt='MySQL'/> [MySQL](http://www.mysql.com) – Databases
- <img width='25' height='25' src='https://img.stackshare.io/service/1028/ASOhU5xJ.png' alt='PostgreSQL'/> [PostgreSQL](http://www.postgresql.org/) – Databases
- <img width='25' height='25' src='https://img.stackshare.io/service/1061/default_df93e9a30d27519161b39d8c1d5c223c1642d187.jpg' alt='RabbitMQ'/> [RabbitMQ](http://www.rabbitmq.com/) – Message Queue
- <img width='25' height='25' src='https://img.stackshare.io/service/1031/default_cbce472cd134adc6688572f999e9122b9657d4ba.png' alt='Redis'/> [Redis](http://redis.io/) – In-Memory Databases
- <img width='25' height='25' src='https://img.stackshare.io/service/11394/appsync.png' alt='Serverless AppSync'/> [Serverless AppSync](https://github.com/serverless-components/aws-app-sync) – GraphQL Tools
- <img width='25' height='25' src='https://img.stackshare.io/service/7624/IG6D4Ro2_400x400.png' alt='Spring Data'/> [Spring Data](https://spring.io/projects/spring-data) – Database Tools
- <img width='25' height='25' src='https://img.stackshare.io/service/2279/jooq-logo-white-750x750-padded.png' alt='jOOQ'/> [jOOQ](http://www.jooq.org) – Database Tools
- <img width='25' height='25' src='https://img.stackshare.io/service/190/CvqrSSFs_400x400.jpg' alt='CircleCI'/> [CircleCI](https://circleci.com/) – Continuous Integration
- <img width='25' height='25' src='https://img.stackshare.io/service/586/n4u37v9t_400x400.png' alt='Docker'/> [Docker](https://www.docker.com/) – Virtual Machine Platforms & Containers
- <img width='25' height='25' src='https://img.stackshare.io/service/3136/docker-compose.png' alt='Docker Compose'/> [Docker Compose](https://github.com/docker/compose) – Container Tools
- <img width='25' height='25' src='https://img.stackshare.io/service/2662/kWjFjx3K_400x400.jpg' alt='FindBugs'/> [FindBugs](http://findbugs.sourceforge.net/) – Code Review
- <img width='25' height='25' src='https://img.stackshare.io/service/11563/actions.png' alt='GitHub Actions'/> [GitHub Actions](https://github.com/features/actions) – Continuous Integration
- <img width='25' height='25' src='https://img.stackshare.io/service/975/gradlephant-social-black-bg.png' alt='Gradle'/> [Gradle](https://www.gradle.org/) – Java Build Tools
- <img width='25' height='25' src='https://img.stackshare.io/service/2645/default_8f9d552b144493679449b16c79647da5787e808b.jpg' alt='Grafana'/> [Grafana](http://grafana.org/) – Monitoring Tools
- <img width='25' height='25' src='https://img.stackshare.io/service/670/jenkins.png' alt='Jenkins'/> [Jenkins](http://jenkins-ci.org/) – Continuous Integration
- <img width='25' height='25' src='https://img.stackshare.io/service/1722/Image_2019-05-20_at_4.53.31_PM.png' alt='Kibana'/> [Kibana](https://www.elastic.co/kibana) – Monitoring Tools
- <img width='25' height='25' src='https://img.stackshare.io/service/2923/05518ecaa42841e834421e9d6987b04f_400x400.png' alt='Logback'/> [Logback](https://logback.qos.ch/) – Log Management
- <img width='25' height='25' src='https://img.stackshare.io/service/10079/loki.png' alt='Loki'/> [Loki](https://github.com/grafana/loki) – Logging Tools
- <img width='25' height='25' src='https://img.stackshare.io/service/2501/default_3cf1b307194b26782be5cb209d30360580ae5b3c.png' alt='Prometheus'/> [Prometheus](http://prometheus.io/) – Monitoring Tools
- <img width='25' height='25' src='https://img.stackshare.io/service/3276/xWt1RFo6_400x400.jpg' alt='Apache Camel'/> [Apache Camel](https://camel.apache.org/) – Platform as a Service
- <img width='25' height='25' src='https://img.stackshare.io/service/6429/alpine_linux.png' alt='Alpine Linux'/> [Alpine Linux](https://www.alpinelinux.org/) – Operating Systems
- <img width='25' height='25' src='https://img.stackshare.io/service/3633/ZBMmmvP1_400x400.jpg' alt='Base'/> [Base](https://getbase.com/) – CRM
- <img width='25' height='25' src='https://img.stackshare.io/service/841/Image_2019-05-20_at_4.58.04_PM.png' alt='Elasticsearch'/> [Elasticsearch](https://www.elastic.co/products/elasticsearch) – Search as a Service
- <img width='25' height='25' src='https://img.stackshare.io/service/4631/default_c2062d40130562bdc836c13dbca02d318205a962.png' alt='Shell'/> [Shell](https://en.wikipedia.org/wiki/Shell_script) – Shells

Full tech stack [here](/techstack.md)

### Useful Docker Commands

>  Start postgres and pgadmin
 ```shell
 docker compose up postgres pgadmin4
 ```
>  Clean up everything using
 ```shell
 docker system prune -a -f --volumes
 ```
>  Claim unused volumes
 ```shell
 docker volume prune
 ```
> Running container
 ```shell
 docker container ls
 ```

### Useful git Commands

How to overwrite local changes with git pull

> Stash local changes:
 ```shell
 $ git stash
 ```
> Pull changes from remote:
 ```shell
 $ git pull
 ```

How to revert the changes that are pushed to remove
```shell
$ git revert $hash
```
