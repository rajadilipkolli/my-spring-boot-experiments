package com.example.demo.readreplica.service;

import com.example.demo.readreplica.domain.ArticleDTO;
import com.example.demo.readreplica.domain.CommentDTO;
import com.example.demo.readreplica.repository.ArticleRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
