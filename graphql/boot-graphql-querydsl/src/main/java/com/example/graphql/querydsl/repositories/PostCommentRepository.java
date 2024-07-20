package com.example.graphql.querydsl.repositories;

import com.example.graphql.querydsl.entities.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface PostCommentRepository
        extends JpaRepository<PostComment, Long>, QuerydslPredicateExecutor<PostComment> {}
