package com.example.graphql.querydsl.repository;

import com.example.graphql.querydsl.entities.Post;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.graphql.data.GraphQlRepository;

@GraphQlRepository
public interface PostRepository CrudRepository<Post, String>, QuerydslPredicateExecutor<Post> {
}