package com.learning.shedlock.repositories;

import com.learning.shedlock.entities.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {}
