package com.example.demo.readreplica.service;

import com.example.demo.readreplica.domain.ArticleDTO;
import com.example.demo.readreplica.domain.CommentDTO;
import com.example.demo.readreplica.entities.Article;
import com.example.demo.readreplica.entities.Comment;
import com.example.demo.readreplica.repository.ArticleRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public Optional<ArticleDTO> findArticleById(Integer id) {
        return this.articleRepository
                .findByArticleId(id)
                .map(
                        article ->
                                new ArticleDTO(
                                        article.getTitle(),
                                        article.getAuthored(),
                                        article.getPublished(),
                                        article.getComments().stream()
                                                .map(
                                                        comment ->
                                                                new CommentDTO(
                                                                        comment.getComment()))
                                                .toList()));
    }

    @Transactional
    public ArticleDTO saveArticle(ArticleDTO articleDTO) {
        Article article = convertToArticle(articleDTO);
        Article savedArticle = this.articleRepository.save(article);
        return new ArticleDTO(
                savedArticle.getTitle(),
                savedArticle.getAuthored(),
                savedArticle.getPublished(),
                savedArticle.getComments().stream()
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
                .collect(Collectors.toList());
    }
}
