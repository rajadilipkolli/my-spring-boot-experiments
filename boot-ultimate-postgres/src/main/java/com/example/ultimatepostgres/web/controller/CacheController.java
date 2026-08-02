package com.example.ultimatepostgres.web.controller;

import com.example.ultimatepostgres.model.CacheRequest;
import com.example.ultimatepostgres.service.CacheService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheService cacheService;

    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @PutMapping("/{key}")
    public ResponseEntity<Void> put(@PathVariable String key, @Valid @RequestBody CacheRequest request) {
        cacheService.put(key, request.getValue(), request.getTtlMillis());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> get(@PathVariable String key) {
        return cacheService
                .get(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> evict(@PathVariable String key) {
        cacheService.evict(key);
        return ResponseEntity.noContent().build();
    }
}
