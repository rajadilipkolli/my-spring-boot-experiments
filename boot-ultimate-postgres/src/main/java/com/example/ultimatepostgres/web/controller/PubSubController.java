package com.example.ultimatepostgres.web.controller;

import com.example.ultimatepostgres.service.IntegrationService;
import com.example.ultimatepostgres.service.PubSubListener;
import com.example.ultimatepostgres.service.PubSubPublisher;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/pubsub")
public class PubSubController {

    private final PubSubPublisher pubSubPublisher;
    private final PubSubListener pubSubListener;
    private final IntegrationService integrationService;

    public PubSubController(
            PubSubPublisher pubSubPublisher, PubSubListener pubSubListener, IntegrationService integrationService) {
        this.pubSubPublisher = pubSubPublisher;
        this.pubSubListener = pubSubListener;
        this.integrationService = integrationService;
    }

    @PostMapping("/publish")
    public ResponseEntity<Void> publish(@RequestBody String message) {
        pubSubPublisher.publish(message);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages")
    public ResponseEntity<List<String>> getMessages() {
        return ResponseEntity.ok(pubSubListener.getReceivedMessages());
    }

    @PostMapping("/combined/{id}")
    public ResponseEntity<Void> executeCombined(@PathVariable String id, @RequestBody JsonNode payload) {
        integrationService.executeCombinedOperation(id, payload);
        return ResponseEntity.ok().build();
    }
}
