package com.example.rest.proxy.repositories;

import com.example.rest.proxy.entities.PostComment;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @EntityGraph(attributePaths = "post")
    List<PostComment> findByPostId(Long postId);
}
