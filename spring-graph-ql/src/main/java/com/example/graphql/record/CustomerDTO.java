package com.example.graphql.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;

import java.util.List;

public record CustomerDTO(@JsonProperty("id") @Id Integer id,
                          @JsonProperty("name") String name,
                          @JsonProperty("orders") List<Order> orders) {
}
