# read-replica-with-spring-boot
This project is an example to show how we can separate read and write operations to primary and replica databases using spring boot and postgresql database.

A read replica is a copy of the master database instance that reflects changes to the master instance in almost real time. We create a replica to offload read requests or analytics traffic from the master.

 - All reads will go to reader instance and writes will go to writer instance

## Liquibase
Use below script for generating the difference in the database

> ./diff.sh

### Reference
 - https://github.com/spring-tips/liquibase