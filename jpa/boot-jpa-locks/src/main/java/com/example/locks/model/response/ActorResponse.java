package com.example.locks.model.response;

import java.time.LocalDate;

public record ActorResponse(Long actorId, String actorName, LocalDate dob, String nationality) {
}
