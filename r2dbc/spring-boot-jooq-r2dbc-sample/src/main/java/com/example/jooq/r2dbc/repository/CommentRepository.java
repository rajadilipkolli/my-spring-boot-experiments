package com.example.jooq.r2dbc.repository;

import com.example.jooq.r2dbc.entities.Comment;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface CommentRepository extends R2dbcRepository<Comment, UUID> {}
