package com.example.demo.readreplica.repository;

import com.example.demo.readreplica.entities.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Integer> {}
