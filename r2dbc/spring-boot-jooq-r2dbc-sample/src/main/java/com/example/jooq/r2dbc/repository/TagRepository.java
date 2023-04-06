package com.example.jooq.r2dbc.repository;

import com.example.jooq.r2dbc.entities.Tags;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface TagRepository extends R2dbcRepository<Tags, UUID> {}
