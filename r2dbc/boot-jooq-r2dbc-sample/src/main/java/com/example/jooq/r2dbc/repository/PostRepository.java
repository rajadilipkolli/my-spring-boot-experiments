package com.example.jooq.r2dbc.repository;

import com.example.jooq.r2dbc.entities.Post;
import com.example.jooq.r2dbc.repository.custom.CustomPostRepository;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface PostRepository extends R2dbcRepository<Post, UUID>, CustomPostRepository {}
