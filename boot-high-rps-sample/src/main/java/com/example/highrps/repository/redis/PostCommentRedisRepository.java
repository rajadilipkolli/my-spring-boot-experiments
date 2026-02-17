package com.example.highrps.repository.redis;

import com.example.highrps.entities.PostCommentRedis;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface PostCommentRedisRepository extends CrudRepository<PostCommentRedis, String> {

    List<PostCommentRedis> findByPostId(Long postId);
}
