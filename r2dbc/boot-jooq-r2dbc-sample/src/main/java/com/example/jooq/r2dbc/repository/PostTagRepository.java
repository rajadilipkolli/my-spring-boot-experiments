package com.example.jooq.r2dbc.repository;

import com.example.jooq.r2dbc.entities.PostTagRelation;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface PostTagRepository extends R2dbcRepository<PostTagRelation, UUID> {}
