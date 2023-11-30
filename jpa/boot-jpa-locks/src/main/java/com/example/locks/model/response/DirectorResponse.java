package com.example.locks.model.response;

import java.time.LocalDate;

public record DirectorResponse(Long directorId, String directorName, LocalDate dob, String nationality) {
}
