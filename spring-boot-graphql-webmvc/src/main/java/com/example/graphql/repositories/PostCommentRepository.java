package com.example.graphql.repositories;

import com.example.graphql.entities.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {}
