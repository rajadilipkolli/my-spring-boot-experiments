package com.example.keysetpagination.web.controllers;

import com.example.keysetpagination.exception.ActorNotFoundException;
import com.example.keysetpagination.model.query.FindActorsQuery;
import com.example.keysetpagination.model.query.SearchCriteria;
import com.example.keysetpagination.model.request.ActorRequest;
import com.example.keysetpagination.model.response.ActorResponse;
import com.example.keysetpagination.model.response.PagedResult;
import com.example.keysetpagination.services.ActorService;
import com.example.keysetpagination.utils.AppConstants;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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

    private final ActorService actorService;

    public ActorController(ActorService actorService) {
        this.actorService = actorService;
    }

    @GetMapping
    public PagedResult<ActorResponse> findAllActors(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @RequestParam(required = false) Long lowest,
            @RequestParam(required = false) Long highest) {

        FindActorsQuery findActorsQuery = new FindActorsQuery(pageNo, pageSize, lowest, highest, sortBy, sortDir);

        return actorService.findAll(findActorsQuery);
    }

    @PostMapping("/search")
    public PagedResult<ActorResponse> searchActors(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @RequestParam(required = false) Long lowest,
            @RequestParam(required = false) Long highest,
            @RequestBody SearchCriteria[] searchCriteria) {

        FindActorsQuery findActorsQuery = new FindActorsQuery(pageNo, pageSize, lowest, highest, sortBy, sortDir);

        return actorService.findAll(searchCriteria, findActorsQuery);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActorResponse> getActorById(@PathVariable Long id) {
        return actorService.findActorById(id).map(ResponseEntity::ok).orElseThrow(() -> new ActorNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<ActorResponse> createActor(@RequestBody @Validated ActorRequest actorRequest) {
        ActorResponse response = actorService.saveActor(actorRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/api/actors/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActorResponse> updateActor(
            @PathVariable Long id, @RequestBody @Valid ActorRequest actorRequest) {
        return ResponseEntity.ok(actorService.updateActor(id, actorRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ActorResponse> deleteActor(@PathVariable Long id) {
        return actorService
                .findActorById(id)
                .map(actor -> {
                    actorService.deleteActorById(id);
                    return ResponseEntity.ok(actor);
                })
                .orElseThrow(() -> new ActorNotFoundException(id));
    }
}
