package com.example.graphql.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.annotation.Id;

public record CustomerDTO(
        @JsonProperty("id") @Id Integer id,
        @JsonProperty("name") String name,
        @JsonProperty("orders") List<Orders> orders) {}
