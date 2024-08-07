package com.example.graphql.querydsl.repositories;

import com.example.graphql.querydsl.entities.Post;
import com.example.graphql.querydsl.entities.QPost;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;
import org.springframework.data.repository.query.Param;

public interface PostRepository
        extends JpaRepository<Post, Long>, QuerydslPredicateExecutor<Post>, QuerydslBinderCustomizer<QPost> {

    @Override
    default void customize(QuerydslBindings bindings, QPost root) {
        bindings.bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
        bindings.excluding(root.id);
    }

    @EntityGraph(attributePaths = {"details", "comments"})
    List<Post> findByDetails_CreatedByEqualsIgnoreCase(String createdBy);

    @Query("select p from Post p left join fetch p.tags where p in :posts ORDER BY p.id")
    List<Post> findAllPostsWithTags(@Param("posts") List<Post> posts);
}
