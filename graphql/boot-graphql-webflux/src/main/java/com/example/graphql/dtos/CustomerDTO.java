package com.example.graphql.dtos;

import java.util.List;
import org.springframework.data.annotation.Id;

public record CustomerDTO(@Id Integer id, String name, List<Orders> orders) {}
