package com.example.demo.readreplica.repository;

import com.example.demo.readreplica.entities.Article;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("select a from Article a left join fetch a.comments where a.id = :articleId  ")
    Optional<Article> findByArticleId(@Param("articleId") Long articleId);
}
