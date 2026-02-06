package com.example.highrps.repository.redis;

import com.example.highrps.entities.AuthorRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRedisRepository extends CrudRepository<AuthorRedis, String> {}
