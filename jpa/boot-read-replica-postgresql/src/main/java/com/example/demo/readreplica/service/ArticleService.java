package com.example.demo.readreplica.service;

import com.example.demo.readreplica.domain.ArticleDTO;
import com.example.demo.readreplica.entities.Article;
import com.example.demo.readreplica.repository.ArticleRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;

    ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public Optional<ArticleDTO> findArticleById(Long id) {
        return this.articleRepository.findByArticleId(id).map(Article::convertToArticleDTO);
    }

    public boolean existsById(Long id) {
        return articleRepository.existsById(id);
    }

    @Transactional
    public Long saveArticle(ArticleDTO articleDTO) {
        Article article = articleDTO.convertToArticle();
        Article savedArticle = this.articleRepository.save(article);
        return savedArticle.getId();
    }

    @Transactional
    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }
}
