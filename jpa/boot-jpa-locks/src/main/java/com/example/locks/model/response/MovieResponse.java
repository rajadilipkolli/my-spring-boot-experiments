package com.example.locks.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MovieResponse(Long movieId, String movieTitle, LocalDate releaseDate, BigDecimal budget,
                            DirectorResponse director, List<ActorResponse> actors,
                            List<ReviewResponse> reviews, List<GenreResponse> genres) {
}
