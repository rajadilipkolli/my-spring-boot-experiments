package com.example.plugin.strategyplugin.controller;

import com.example.plugin.strategyplugin.domain.GenericDTO;
import com.example.plugin.strategyplugin.service.StrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class StrategyController {

    private static final Logger logger = LoggerFactory.getLogger(StrategyController.class);

    private final StrategyService strategyService;

    StrategyController(StrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @GetMapping("/fetch")
    GenericDTO fetchData(@RequestParam("type") String type) {
        logger.info("Requested to fetch of type :{}", type);
        return this.strategyService.fetchData(type);
    }
}
