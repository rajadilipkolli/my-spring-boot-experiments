package com.example.highrps.repository.redis;

import com.example.highrps.entities.PostRedis;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRedisRepository extends CrudRepository<PostRedis, String> {
    Optional<PostRedis> findByTitleAndAuthorEmailIgnoreCase(String title, String authorEmail);

    long deleteByTitleAndAuthorEmailIgnoreCase(String title, String authorEmail);
}
