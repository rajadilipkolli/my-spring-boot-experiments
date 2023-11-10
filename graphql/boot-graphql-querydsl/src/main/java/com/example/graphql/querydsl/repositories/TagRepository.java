package com.example.graphql.querydsl.repositories;

import com.example.graphql.querydsl.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {}
