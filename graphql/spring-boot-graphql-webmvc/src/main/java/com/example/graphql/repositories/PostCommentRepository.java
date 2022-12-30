package com.example.graphql.repositories;

import com.example.graphql.entities.PostComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPost_IdIn(List<Long> ids);
}
