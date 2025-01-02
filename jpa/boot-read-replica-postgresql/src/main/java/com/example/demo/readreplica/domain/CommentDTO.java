package com.example.demo.readreplica.domain;

import com.example.demo.readreplica.entities.Comment;

public record CommentDTO(String comment) {

    static Comment convertToComment(CommentDTO commentDTO) {
        return new Comment().setComment(commentDTO.comment());
    }
}
