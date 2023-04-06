package com.example.jooq.r2dbc.repository;

import com.example.jooq.r2dbc.entities.Comment;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface CommentRepository extends R2dbcRepository<Comment, UUID> {}
