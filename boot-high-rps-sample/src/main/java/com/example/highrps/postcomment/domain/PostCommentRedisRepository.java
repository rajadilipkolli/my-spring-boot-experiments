package com.example.highrps.postcomment.domain;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface PostCommentRedisRepository extends CrudRepository<PostCommentRedis, String> {

    List<PostCommentRedis> findByPostId(Long postId);
}
