[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/rajadilipkolli/my-spring-boot-experiments)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)


# spring boot integration experiments

The following table list all sample codes related to the spring boot integrations.

| Name                                                                                            | Description 		                                                                                                                                                                 | Status 		 |
|-------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| [Ultimate Redis Implementation](./spring-boot-ultimate-redis)                             | The application, discovers ways of interacting with Redis with different TTLs for different Caches                                                                             | Completed |
| [Connecting to multiple data sources](./spring-boot-multipledatasources)                  | The application, demonstrates the way spring boot application connects with multiple databases,connection pooling and both type of database migrations (liquibase and flyway)  | Completed |
| [Implementation of Strategy Design Pattern](./spring-boot-strategy-plugin)                | The application, demonstrates strategy pattern in spring application and build native image using GraalVM, use a main class to start up the application and builds nativeImage | Completed |
| [Archunit Implementation](./spring-boot-api-archunit-sample)                              | The application, demonstrates how to apply arch unit rules to the spring boot project                                                                                          | Completed |
| [Hibernate Envers Implementation using spring data JPA](./spring-boot-data-envers-sample) | The application, demonstrates how to apply hibernate envers to the spring boot project                                                                                         | Completed |
| [Graph QL implementation using webflux](./spring-boot-graphql-webflux)                    | The application, demonstrates the way to connect to database using graph ql using webflux                                                                                      | Completed |
| [Hibernate 2nd Level Cache Using Redis](./spring-boot-hibernate2ndlevelcache-sample)      | The application, demonstrates how to apply Hibernate 2nd level cache using redis in a spring boot project , testing using QueryCounting                                        | Completed |
| [Read Replica Postgres](./spring-boot-read-replica-postgresql)                            | The application, demonstrates saving the data in Posrgresql and then read from replica instance                                                                                | Completed |
| [BackgroundJobs and Scheduling using Jobrunr](./spring-boot-jobrunr-sample)               | The application, demonstrates running background jobs and scheduling the tasks using [Jobrunr](https://www.jobrunr.io/en/)                                                     | Completed |
| [MultiTenancy DB Based](./spring-boot-jpa-multitenancy/db)                                | The application, demonstrates running multi tenancy in JPA using different databases but same DDLs and DMLs                                                                    | Completed |
| [MultiTenancy Partition Based](./spring-boot-jpa-multitenancy/partition)                  | The application, demonstrates running multi tenancy in JPA using partition based i.e Shared Database with Shared table                                                         | Completed |
| [MultiTenancy Schema Based](./spring-boot-jpa-multitenancy/schema)                        | The application, demonstrates running multi tenancy in JPA using schema based i.e Shared Database with Separate Schema                                                         | Completed |
| [MultiTenancy with multipledatsources](./spring-boot-jpa-multitenancy/multidatasource-multitenancy)  | The application, demonstrates running multi tenancy in JPA using all strategies using multidatasources                                                                     | Completed |
| [mongodb-elasticsearch-integration](./spring-boot-mongodb-elasticsearch)                  | The application, demonstrates saving the data in MongoDb and then searching in ElasticSearch for quick Search, GeoSpatial Search                                               | WIP       |
| [my-spring-graphql-querydsl](./my-spring-graphql-querydsl)                                | The application, demonstrates the way to connect to database using graph ql and querydsl                                                                                       | WIP       |
| [my-spring-graphql-webmvc](./my-spring-graphql-webmvc)                                    | The application, demonstrates how to apply graphql concepts to the spring boot project                                                                                         | WIP       |
| [Choas Engineering Principles](./spring-boot-choas-monkey)                                | The application, demonstrates how to apply choas engineering concepts to the spring boot project, test using Gatling to demonstrate the difference                             | WIP       |
| [Feature Toggles](./spring-boot-togglz-sample)                                            | The application, demonstrates how to apply feature toggles concepts to the spring boot project                                                                                 | WIP       |

For More info about this repository, Please visit [here](https://rajadilipkolli.github.io/my-spring-boot-experiments/)


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
