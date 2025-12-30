package com.example.highrps.controller;

import com.example.highrps.model.EventDto;
import com.example.highrps.model.StatsResponse;
import com.example.highrps.service.HelloService;
import com.example.highrps.service.KafkaProducerService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private final HelloService service;
    private final KafkaProducerService kafkaProducerService;

    public HelloController(HelloService service, KafkaProducerService kafkaProducerService) {
        this.service = service;
        this.kafkaProducerService = kafkaProducerService;
    }

    @GetMapping(value = "/ping", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ping() {
        return service.ping();
    }

    @GetMapping(value = "/stats/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public StatsResponse stats(@PathVariable String id) {
        return service.getStats(id);
    }

    @GetMapping(value = "/publish/{id}")
    public String publish(@PathVariable String id) {
        var ev = new EventDto(id, System.currentTimeMillis());
        kafkaProducerService.publishEvent(ev);
        return "ok";
    }

    @PostMapping(value = "/events", consumes = "application/json")
    public String publishEvent(@RequestBody EventDto ev) {
        kafkaProducerService.publishEvent(ev);
        return "accepted";
    }
}
