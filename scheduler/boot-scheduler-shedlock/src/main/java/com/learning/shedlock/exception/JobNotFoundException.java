package com.learning.shedlock.exception;

public class JobNotFoundException extends ResourceNotFoundException {

    public JobNotFoundException(Long id) {
        super("Job with Id '%d' not found".formatted(id));
    }
}
