package com.example.graphql.dtos;

import org.springframework.data.annotation.Id;

public record Orders(@Id Integer id, Integer customerId) {}
