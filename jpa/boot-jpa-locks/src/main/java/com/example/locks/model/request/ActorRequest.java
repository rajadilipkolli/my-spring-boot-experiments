package com.example.locks.model.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record ActorRequest(
        @NotBlank(message = "ActorName cant be Blank") String actorName, LocalDate dob, String nationality) {}
