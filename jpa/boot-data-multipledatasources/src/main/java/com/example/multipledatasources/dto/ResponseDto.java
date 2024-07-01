package com.example.multipledatasources.dto;

import java.io.Serializable;

public record ResponseDto(String memberId, String cardNumber, String memberName) implements Serializable {}
