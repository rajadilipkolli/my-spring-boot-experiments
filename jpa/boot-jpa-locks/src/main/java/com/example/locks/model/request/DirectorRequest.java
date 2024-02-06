package com.example.locks.model.request;

import java.time.LocalDate;

public record DirectorRequest(String directorName, LocalDate dob, String nationality) {
}
