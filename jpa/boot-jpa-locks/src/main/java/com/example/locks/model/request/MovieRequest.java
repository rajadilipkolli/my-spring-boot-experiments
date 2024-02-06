package com.example.locks.model.request;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MovieRequest(
        @NotBlank(message = "MovieTitle cant be Blank") String movieTitle,
        LocalDate releaseDate,
        BigDecimal budget,
        DirectorRequest director,
        List<ActorRequest> actors,
        List<ReviewRequest> reviews,
        List<GenreRequest> genres) {}
