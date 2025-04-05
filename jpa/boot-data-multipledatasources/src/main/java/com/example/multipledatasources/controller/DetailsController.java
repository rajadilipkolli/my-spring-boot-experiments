package com.example.multipledatasources.controller;

import com.example.multipledatasources.dto.ResponseDto;
import com.example.multipledatasources.service.DetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DetailsController {

    private final DetailsService detailsService;

    public DetailsController(DetailsService detailsService) {
        this.detailsService = detailsService;
    }

    @GetMapping("/details/{memberId}")
    ResponseDto getDetails(@PathVariable String memberId) {
        return detailsService.getDetails(memberId);
    }
}
