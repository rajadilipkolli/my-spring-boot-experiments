package com.example.demo.readreplica.controller;

import com.example.demo.readreplica.domain.ArticleDTO;
import com.example.demo.readreplica.service.ArticleService;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/articles")
class ArticleController {

    private final ArticleService articleService;

    ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/{id}")
    ResponseEntity<ArticleDTO> findArticleById(@PathVariable Long id) {
        return this.articleService
                .findArticleById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/")
    ResponseEntity<Object> saveArticle(@RequestBody ArticleDTO articleDTO) {
        Long articleId = this.articleService.saveArticle(articleDTO);
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("{id}")
                        .buildAndExpand(articleId)
                        .toUri();
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Object> deleteArticle(@PathVariable Long id) {
        return this.articleService
                .findById(id)
                .map(
                        article -> {
                            articleService.deleteById(article.getId());
                            return ResponseEntity.accepted().build();
                        })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
