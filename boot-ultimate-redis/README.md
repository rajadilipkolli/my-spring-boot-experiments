# boot-ultimate-redis
This is an example repository to discover ways of interacting with Redis (from a Spring Boot 2 Application).

The blog post about this repository can be found [HERE](https://programmerfriend.com/ultimate-guide-to-redis-cache-with-spring-boot-2-and-spring-data-redis/?gthb).

![Spring Boot Redis](https://github.com/programmerfriend/programmerfriend.github.io/blob/master/img/content/robust-boot_title.png?raw=true "Spring Boot Redis")

## View Keys using cli

Connect to redis-cli using below command
```shell
docker exec -it redis-server redis-cli
```

Run keys * command to view all keys
```shell
keys *
```

## Run the service
```shell
./mvnw spring-boot:run
```

## What it is / What it does
* A Spring Boot 3 Application
* Using @Cacheable, @CachePut, @CacheEvict to cache results of method invocations
* Use Redis to store the cached results
* Define different TTLs for different Caches
