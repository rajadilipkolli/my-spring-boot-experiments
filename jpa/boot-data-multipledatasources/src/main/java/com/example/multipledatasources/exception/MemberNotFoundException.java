package com.example.multipledatasources.exception;

public class MemberNotFoundException extends ResourceNotFoundException {

    public MemberNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
