package com.example.bootr2dbc.repositories;

import com.example.bootr2dbc.entities.ReactivePost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactivePostRepository extends JpaRepository<ReactivePost, Long> {}
