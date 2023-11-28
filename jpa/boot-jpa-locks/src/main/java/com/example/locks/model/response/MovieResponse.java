package com.example.locks.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovieResponse(Long movieId, String movieTitle, LocalDate releaseDate, BigDecimal budget) {}
