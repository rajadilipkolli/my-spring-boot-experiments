package com.example.demo.readreplica.controller;

import com.example.demo.readreplica.domain.ArticleDTO;
import com.example.demo.readreplica.service.ArticleService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/{id}")
    public Optional<ArticleDTO> findArticleById(@PathVariable Integer id) {
        return this.articleService.findArticleById(id);
    }

    @PostMapping("/")
    public ArticleDTO saveArticle(@RequestBody ArticleDTO articleDTO) {
        return this.articleService.saveArticle(articleDTO);
    }
}
