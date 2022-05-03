package com.example.graphql.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;

public record Orders(
        @JsonProperty("id") @Id Integer id, @JsonProperty("customerId") Integer customerId) {}
