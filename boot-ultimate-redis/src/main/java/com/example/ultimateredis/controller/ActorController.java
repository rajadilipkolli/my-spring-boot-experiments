package com.example.ultimateredis.controller;

import com.example.ultimateredis.model.Actor;
import com.example.ultimateredis.model.ActorRequest;
import com.example.ultimateredis.model.GenericResponse;
import com.example.ultimateredis.service.ActorService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/actors")
public class ActorController {

    private static final Logger log = LoggerFactory.getLogger(ActorController.class);
    private final ActorService actorService;

    public ActorController(ActorService actorService) {
        this.actorService = actorService;
    }

    @GetMapping
    public ResponseEntity<GenericResponse<List<Actor>>> getAllActors() {
        log.info("Fetching all actors");
        List<Actor> actors = actorService.findAll();
        return ResponseEntity.ok(new GenericResponse<>(actors));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse<Actor>> getActorById(@PathVariable String id) {
        log.info("Fetching actor with id: {}", id);
        return actorService
                .findActorById(id)
                .map(actor -> ResponseEntity.ok(new GenericResponse<>(actor)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/by-name")
    public ResponseEntity<GenericResponse<Actor>> getActorByName(@RequestParam String name) {
        log.info("Fetching actor with name: {}", name);
        return actorService
                .findActorByName(name)
                .map(actor -> ResponseEntity.ok(new GenericResponse<>(actor)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/by-name-and-age")
    public ResponseEntity<GenericResponse<Actor>> getActorByNameAndAge(
            @RequestParam String name, @RequestParam int age) {
        log.info("Fetching actor with name: {} and age: {}", name, age);
        return actorService
                .findActorByNameAndAge(name, age)
                .map(actor -> ResponseEntity.ok(new GenericResponse<>(actor)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<GenericResponse<Actor>> createActor(
            @Valid @RequestBody ActorRequest actorRequest) {
        log.info("Creating actor: {}", actorRequest);
        Actor actor =
                new Actor(UUID.randomUUID().toString(), actorRequest.name(), actorRequest.age());
        Actor savedActor = actorService.saveActor(actor);

        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(savedActor.getId())
                        .toUri();

        return ResponseEntity.created(location).body(new GenericResponse<>(savedActor));
    }

    @PostMapping("/batch")
    public ResponseEntity<GenericResponse<List<Actor>>> createActors(
            @Valid @RequestBody List<ActorRequest> requests) {
        log.info("Creating multiple actors: {}", requests);
        List<Actor> actors =
                requests.stream()
                        .map(req -> new Actor(UUID.randomUUID().toString(), req.name(), req.age()))
                        .toList();

        List<Actor> savedActors = actorService.saveActors(actors);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse<>(savedActors));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse<Actor>> updateActor(
            @PathVariable String id, @Valid @RequestBody ActorRequest actorRequest) {
        log.info("Updating actor with id: {}", id);

        Optional<Actor> existingActorOpt = actorService.findActorById(id);
        if (existingActorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Actor existingActor = existingActorOpt.get();
        existingActor.setName(actorRequest.name());
        existingActor.setAge(actorRequest.age());

        Actor updatedActor = actorService.saveActor(existingActor);
        return ResponseEntity.ok(new GenericResponse<>(updatedActor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActor(@PathVariable String id) {
        log.info("Deleting actor with id: {}", id);

        if (actorService.findActorById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        actorService.deleteActorById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/by-name")
    public ResponseEntity<Void> deleteActorsByName(@RequestParam String name) {
        log.info("Deleting actors with name: {}", name);
        actorService.deleteActorByName(name);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllActors() {
        log.info("Deleting all actors");
        actorService.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
