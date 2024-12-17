package com.example.keysetpagination.web.controllers;

import com.example.keysetpagination.exception.AnimalNotFoundException;
import com.example.keysetpagination.model.query.FindAnimalsQuery;
import com.example.keysetpagination.model.query.SearchCriteria;
import com.example.keysetpagination.model.request.AnimalRequest;
import com.example.keysetpagination.model.response.AnimalResponse;
import com.example.keysetpagination.model.response.PagedResult;
import com.example.keysetpagination.services.AnimalService;
import com.example.keysetpagination.utils.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Window;
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
@RequestMapping("/api/animals")
@Validated
class AnimalController {

    private final AnimalService animalService;

    public AnimalController(AnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping
    PagedResult<AnimalResponse> getAllAnimals(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false)
                    int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false)
                    int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false)
                    String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false)
                    String sortDir) {
        FindAnimalsQuery findAnimalsQuery = new FindAnimalsQuery(pageNo, pageSize, sortBy, sortDir);
        return animalService.findAllAnimals(findAnimalsQuery);
    }

    @Operation(summary = "Search animals with keyset pagination support")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved animals"),
                @ApiResponse(responseCode = "400", description = "Invalid parameters supplied"),
                @ApiResponse(responseCode = "404", description = "No animals found matching criteria")
            })
    @PostMapping("/search")
    public Window<AnimalResponse> searchAnimals(
            @Parameter(description = "Number of items per page (max 100)")
                    @RequestParam(defaultValue = "10")
                    @Min(1)
                    @Max(100)
                    int pageSize,
            @Parameter(description = "Scroll ID for pagination") @RequestParam(required = false) Long scrollId,
            @RequestBody @Valid List<SearchCriteria> searchCriteria) {

        return animalService.searchAnimals(searchCriteria, pageSize, scrollId);
    }

    @GetMapping("/{id}")
    ResponseEntity<AnimalResponse> getAnimalById(@PathVariable Long id) {
        return animalService
                .findAnimalById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new AnimalNotFoundException(id));
    }

    @PostMapping
    ResponseEntity<AnimalResponse> createAnimal(@RequestBody @Valid AnimalRequest animalRequest) {
        AnimalResponse response = animalService.saveAnimal(animalRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    ResponseEntity<AnimalResponse> updateAnimal(
            @PathVariable Long id, @RequestBody @Valid AnimalRequest animalRequest) {
        return ResponseEntity.ok(animalService.updateAnimal(id, animalRequest));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<AnimalResponse> deleteAnimal(@PathVariable Long id) {
        return animalService
                .findAnimalById(id)
                .map(animal -> {
                    animalService.deleteAnimalById(id);
                    return ResponseEntity.ok(animal);
                })
                .orElseThrow(() -> new AnimalNotFoundException(id));
    }
}
