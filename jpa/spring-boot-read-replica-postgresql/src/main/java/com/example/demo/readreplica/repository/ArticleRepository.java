package com.example.demo.readreplica.repository;

import com.example.demo.readreplica.entities.Article;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Integer> {

    @Transactional(readOnly = true)
    @Query("select a from Article a join fetch a.comments where a.id = :articleId  ")
    Optional<Article> findByArticleId(@Param("articleId") Integer articleId);
}
