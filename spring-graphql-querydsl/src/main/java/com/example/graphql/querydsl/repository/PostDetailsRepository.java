package com.example.graphql.querydsl.repository;

import com.example.graphql.querydsl.entities.PostDetails;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.graphql.data.GraphQlRepository;

@GraphQlRepository
public interface PostDetailsRepository
        extends CrudRepository<PostDetails, Long>, QuerydslPredicateExecutor<PostDetails> {
    long countByPost_Details_CreatedByIgnoreCase(String createdBy);
}
