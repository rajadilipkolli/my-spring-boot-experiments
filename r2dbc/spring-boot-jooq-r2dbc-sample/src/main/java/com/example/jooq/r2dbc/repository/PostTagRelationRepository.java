package com.example.jooq.r2dbc.repository;

import com.example.jooq.r2dbc.entities.PostTagRelation;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface PostTagRelationRepository extends R2dbcRepository<PostTagRelation, UUID> {}
