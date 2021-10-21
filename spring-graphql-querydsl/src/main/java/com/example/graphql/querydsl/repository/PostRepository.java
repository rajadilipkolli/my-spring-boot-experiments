package com.example.graphql.querydsl.repository;

import com.example.graphql.querydsl.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.graphql.data.GraphQlRepository;

@GraphQlRepository
public interface PostRepository extends JpaRepository<Post, Long> {
}