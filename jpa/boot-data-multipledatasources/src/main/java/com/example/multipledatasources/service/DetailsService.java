package com.example.multipledatasources.service;

import com.example.multipledatasources.dto.ResponseDto;

public interface DetailsService {
    ResponseDto getDetails(String memberId);
}
