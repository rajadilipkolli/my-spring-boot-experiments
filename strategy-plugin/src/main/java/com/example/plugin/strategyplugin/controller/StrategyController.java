package com.example.plugin.strategyplugin.controller;

import com.example.plugin.strategyplugin.service.StrategyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public record StrategyController(StrategyService strategyService) {

  @GetMapping("/fetch")
  String fetchData(@RequestParam("type") String type) {
    return this.strategyService.fetchData(type);
  }
}
