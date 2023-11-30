package com.example.locks.model.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovieRequest(String movieTitle, LocalDate releaseDate, BigDecimal budget,
                           DirectorRequest director) {
}
