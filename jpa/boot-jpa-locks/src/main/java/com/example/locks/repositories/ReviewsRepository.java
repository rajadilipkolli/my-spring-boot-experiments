package com.example.locks.repositories;

import com.example.locks.entities.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewsRepository extends JpaRepository<Reviews, Long> {}
