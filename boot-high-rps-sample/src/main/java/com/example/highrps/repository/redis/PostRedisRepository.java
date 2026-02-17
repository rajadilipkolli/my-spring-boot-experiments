package com.example.highrps.repository.redis;

import com.example.highrps.entities.PostRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRedisRepository extends CrudRepository<PostRedis, Long> {}
