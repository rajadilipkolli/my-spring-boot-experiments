package com.example.demo.readreplica.domain;

import com.example.demo.readreplica.entities.Article;
import java.time.LocalDateTime;
import java.util.List;

public record ArticleDTO(
        String title,
        LocalDateTime authored,
        LocalDateTime published,
        List<CommentDTO> commentDTOs) {

    public Article convertToArticle() {
        Article article =
                new Article().setAuthored(authored).setTitle(title).setPublished(published);
        commentDTOs.stream().map(CommentDTO::convertToComment).forEach(article::addComment);
        return article;
    }
}
