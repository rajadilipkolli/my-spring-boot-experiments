package com.example.demo.readreplica.domain;

import java.time.LocalDateTime;
import java.util.List;

public record ArticleDTO(
        String title,
        LocalDateTime authored,
        LocalDateTime published,
        List<CommentDTO> commentDTOs) {}
