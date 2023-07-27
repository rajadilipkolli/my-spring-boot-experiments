package com.example.envers.model;

import com.example.envers.entities.Customer;
import java.time.Instant;
import java.util.Optional;

public record RevisionDTO(
        Customer entity, Optional<Integer> revisionNumber, String revisionType, Optional<Instant> revisionInstant) {}
