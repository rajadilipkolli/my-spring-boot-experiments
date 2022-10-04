package com.example.graphql.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;

public record Customer(@JsonProperty("id") @Id Integer id, @JsonProperty("name") String name) {}
