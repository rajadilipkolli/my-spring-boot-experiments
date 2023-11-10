package com.example.graphql.querydsl.repositories;

import com.example.graphql.querydsl.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {}
