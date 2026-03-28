package com.example.highrps.author.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRedisRepository extends CrudRepository<AuthorRedis, String> {}
