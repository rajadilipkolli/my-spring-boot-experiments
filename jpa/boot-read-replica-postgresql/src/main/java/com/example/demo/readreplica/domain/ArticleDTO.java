package com.example.demo.readreplica.domain;

import static com.example.demo.readreplica.domain.CommentDTO.convertToComment;

import com.example.demo.readreplica.entities.Article;
import com.example.demo.readreplica.entities.Comment;
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
        commentDTOs.forEach(
                commentDTO -> {
                    Comment comment = convertToComment(commentDTO);
                    article.addComment(comment);
                });
        return article;
    }
}
