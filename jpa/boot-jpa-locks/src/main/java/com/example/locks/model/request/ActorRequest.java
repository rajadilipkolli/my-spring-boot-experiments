package com.example.locks.model.request;

import java.time.LocalDate;

public record ActorRequest(String actorName, LocalDate dob, String nationality) {
}
