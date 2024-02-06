package com.example.locks.model.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MovieRequest(String movieTitle, LocalDate releaseDate, BigDecimal budget,
                           DirectorRequest director, List<ActorRequest> actors,
                           List<ReviewRequest> reviews, List<GenreRequest> genres) {
}
