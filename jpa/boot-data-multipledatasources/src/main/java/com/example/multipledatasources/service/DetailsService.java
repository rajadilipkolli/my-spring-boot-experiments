package com.example.multipledatasources.service;

import com.example.multipledatasources.dto.ResponseDto;
import com.example.multipledatasources.exception.CustomServiceException;

public interface DetailsService {
    ResponseDto getDetails(String memberId) throws CustomServiceException;
}
