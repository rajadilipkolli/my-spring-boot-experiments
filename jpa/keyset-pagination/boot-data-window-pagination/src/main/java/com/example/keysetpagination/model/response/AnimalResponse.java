package com.example.keysetpagination.model.response;

import java.time.LocalDateTime;

public record AnimalResponse(Long id, String name, String type, String habitat, LocalDateTime created) {}
