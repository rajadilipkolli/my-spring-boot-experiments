package com.example.locks.web.controllers;

import com.example.locks.exception.MovieNotFoundException;
import com.example.locks.model.response.ActorResponse;
import com.example.locks.model.response.MovieResponse;
import com.example.locks.services.ActorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/actors")
@Slf4j
@RequiredArgsConstructor
public class ActorController {

    private final ActorService actorService;

    @GetMapping("/{id}")
    public ResponseEntity<ActorResponse> getActorById(@PathVariable Long id) {
        return actorService.findActor(id).map(ResponseEntity::ok).orElseThrow(() -> new ActorNotFoundException(id));
    }
}
