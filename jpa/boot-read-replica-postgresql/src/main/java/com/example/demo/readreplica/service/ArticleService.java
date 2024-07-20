package com.example.demo.readreplica.service;

import com.example.demo.readreplica.domain.ArticleDTO;
import com.example.demo.readreplica.domain.CommentDTO;
import com.example.demo.readreplica.entities.Article;
import com.example.demo.readreplica.entities.Comment;
import com.example.demo.readreplica.repository.ArticleRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public Optional<ArticleDTO> findArticleById(Integer id) {
        return this.articleRepository.findByArticleId(id).map(this::convertToArticleDTO);
    }

    @Transactional
    public Long saveArticle(ArticleDTO articleDTO) {
        Article article = convertToArticle(articleDTO);
        Article savedArticle = this.articleRepository.save(article);
        return savedArticle.getId();
    }

    private ArticleDTO convertToArticleDTO(Article articleEntity) {
        return new ArticleDTO(
                articleEntity.getTitle(),
                articleEntity.getAuthored(),
                articleEntity.getPublished(),
                articleEntity.getComments().stream()
                        .map(comment -> new CommentDTO(comment.getComment()))
                        .toList());
    }

    private Article convertToArticle(ArticleDTO articleDTO) {
        Article article = new Article();
        article.setAuthored(articleDTO.authored());
        article.setTitle(articleDTO.title());
        article.setPublished(articleDTO.published());
        convertToComment(articleDTO.commentDTOs()).forEach(article::addComment);
        return article;
    }

    private List<Comment> convertToComment(List<CommentDTO> commentDTOs) {
        return commentDTOs.stream()
                .map(
                        commentDTO -> {
                            Comment comment = new Comment();
                            comment.setComment(commentDTO.comment());
                            return comment;
                        })
                .toList();
    }
}
