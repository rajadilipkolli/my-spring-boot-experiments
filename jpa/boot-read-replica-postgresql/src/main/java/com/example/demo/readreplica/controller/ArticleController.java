package com.example.demo.readreplica.controller;

import com.example.demo.readreplica.domain.ArticleDTO;
import com.example.demo.readreplica.service.ArticleService;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles")
class ArticleController {

    private final ArticleService articleService;

    ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/{id}")
    ResponseEntity<ArticleDTO> findArticleById(@PathVariable Integer id) {
        return this.articleService
                .findArticleById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/")
    ResponseEntity<Object> saveArticle(@RequestBody ArticleDTO articleDTO) {
        Long articleId = this.articleService.saveArticle(articleDTO);
        return ResponseEntity.created(URI.create("/articles/" + articleId)).build();
    }
}
