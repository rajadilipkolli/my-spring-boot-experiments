package com.example.jooq.r2dbc.repository;

import com.example.jooq.r2dbc.entities.Tags;
import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface TagRepository extends R2dbcRepository<Tags, UUID> {}
