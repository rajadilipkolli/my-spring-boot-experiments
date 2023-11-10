package com.example.graphql.querydsl.repositories;

import com.example.graphql.querydsl.entities.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {}
