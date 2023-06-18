package com.example.multipledatasources.controller;

import com.example.multipledatasources.dto.ResponseDto;
import com.example.multipledatasources.service.DetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DetailsController {

    private final DetailsService detailsService;

    @GetMapping("/details/{memberId}")
    ResponseDto getDetails(@PathVariable("memberId") String memberId) {
        return detailsService.getDetails(memberId);
    }
}
