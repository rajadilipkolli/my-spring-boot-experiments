[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/rajadilipkolli/my-spring-boot-experiments)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)


# spring boot integration experiments

The following table list all sample codes related to the spring boot integrations.

| Name                                                                                            | Description 		                                                                                                                                                                | Status 		 |
|-------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| [Ultimate Redis Implementation](../main/spring-boot-ultimate-redis)                             | The application, discovers ways of interacting with Redis with different TTLs for different Caches                                                                            | Completed |
| [Connecting to multiple data sources](../main/spring-boot-multipledatasources)                  | The application, demonstrates the way spring boot application connects with multiple databases,connection pooling and both type of database migrations (liquibase and flyway) | Completed |
| [Implementation of Strategy Design Pattern](../main/spring-boot-strategy-plugin)                | The application, demonstrates strategy pattern in spring application and build native image using GraalVM, use a main class to start up the application                       | Completed |
| [Archunit Implementation](../main/spring-boot-api-archunit-sample)                              | The application, demonstrates how to apply arch unit rules to the spring boot project                                                                                         | Completed |
| [Hibernate Envers Implementation using spring data JPA](../main/spring-boot-data-envers-sample) | The application, demonstrates how to apply hibernate envers to the spring boot project                                                                                        | Completed |
| [Graph QL implementation using webflux](../main/spring-boot-graphql-webflux)                    | The application, demonstrates the way to connect to database using graph ql using webflux                                                                                     | Completed |
| [Hibernate 2nd Level Cache Using Redis](../main/spring-boot-hibernate2ndlevelcache-sample)      | The application, demonstrates how to apply Hibernate 2nd level cache using redis in a spring boot project , testing using QueryCounting                                       | Completed |
| [Read Replica Postgres](../main/spring-boot-read-replica-postgresql)                            | The application, demonstrates saving the data in Posrgresql and then read from replica instance                                                                               | Completed |
| [BackgroundJobs and Scheduling using Jobrunr](../main/spring-boot-jobrunr-sample)               | The application, demonstrates running backgroundjobs and scheduling the tasks using [Jobrunr](https://www.jobrunr.io/en/)                                                     | Completed |
| [MultiTenancy DB Based](../main/spring-boot-jpa-multitenancy/db)                                | The application, demonstrates running multi tenancy in JPA using different databases but same DDLs and DMLs                                                                   | Completed |
| [MultiTenancy Partition Based](../main/spring-boot-jpa-multitenancy/partition)                  | The application, demonstrates running multi tenancy in JPA using partition based i.e Shared Database with Shared table                                                        | Completed |
| [MultiTenancy Schema Based](../main/spring-boot-jpa-multitenancy/schema)                        | The application, demonstrates running multi tenancy in JPA using schema based i.e Shared Database with Saperate Schema                                                        | Completed |
| [mongodb-elasticsearch-integration](../main/spring-boot-mongodb-elasticsearch)                  | The application, demonstrates saving the data in MongoDb and then searching in ElasticSearch for quick Search, GeoSpatial Search                                              | WIP       |
| [my-spring-graphql-querydsl](../main/my-spring-graphql-querydsl)                                | The application, demonstrates the way to connect to database using graph ql and querydsl                                                                                      | WIP       |
| [my-spring-graphql-webmvc](../main/my-spring-graphql-webmvc)                                    | The application, demonstrates how to apply graphql concepts to the spring boot project                                                                                        | WIP       |
| [Choas Engineering Principles](../main/spring-boot-choas-monkey)                                      | The application, demonstrates how to apply choas engineering concepts to the spring boot project, test using Gatling to demonstrate the difference                                                                          | WIP       |
| [Feature Toggles](../main/spring-boot-togglz-sample)                                            | The application, demonstrates how to apply feature toggles concepts to the spring boot project                                                                                | WIP       |

For More info about this repository, Please visit [here](https://rajadilipkolli.github.io/my-spring-boot-experiments/)


### Useful Docker Commands

 >  Start postgres and pgadmin 
 ```
 docker compose up postgres pgadmin4
 ```
 >  Clean up everything using 
 ```
 docker system prune -a -f --volumes
 ```
 >  Claim unused volumes 
 ```
 docker volume prune
 ```
 > Running container
 ```
 docker container ls
 ```
> How to overwrite local changes with git pull

Stash local changes: `$ git stash`

Pull changes from remote: `$ git pull`
