package com.example.graphql.querydsl.repository;

import com.example.graphql.querydsl.entities.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.graphql.data.GraphQlRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

@GraphQlRepository
public interface PostRepository
    extends CrudRepository<Post, Long>, QuerydslPredicateExecutor<Post> {
    @EntityGraph(attributePaths = {"details", "comments"})
    List<Post> findByDetails_CreatedByEqualsIgnoreCase(@NonNull String createdBy);

    @EntityGraph(attributePaths = {"tags.tag"})
    @Override
    List<Post> findAllById(Iterable<Long> longs);
}
