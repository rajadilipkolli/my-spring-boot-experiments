package com.example.mongoes.web.api;

import com.example.mongoes.document.Restaurant;
import com.example.mongoes.model.request.GradesRequest;
import com.example.mongoes.model.request.RestaurantRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@Tag(name = "Restaurant Management", description = "APIs for managing restaurant information")
public interface RestaurantApi {

    @Operation(
            summary = "Find all restaurants",
            description = "Returns a paginated list of restaurants",
            parameters = {
                @Parameter(
                        name = "limit",
                        description = "Maximum number of items per page",
                        example = "10"),
                @Parameter(
                        name = "offset",
                        description = "Starting position in pagination",
                        example = "0")
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved restaurants",
                        content = @Content(schema = @Schema(implementation = SearchPage.class))),
                @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
            })
    @GetMapping
    Mono<ResponseEntity<SearchPage<Restaurant>>> findAllRestaurants(
            @Valid @RequestParam(defaultValue = "10") @Max(999) int limit,
            @RequestParam(defaultValue = "0") int offset);

    @Operation(
            summary = "Find restaurant by name",
            description = "Returns a restaurant matching the exact name",
            parameters = {
                @Parameter(
                        name = "restaurantName",
                        description = "Name of the restaurant",
                        example = "Pizza Hut")
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Restaurant found",
                        content = @Content(schema = @Schema(implementation = Restaurant.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Restaurant not found",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                @ApiResponse(responseCode = "400", description = "Invalid restaurant name format")
            })
    @GetMapping("/name/{restaurantName}")
    Mono<ResponseEntity<Restaurant>> findRestaurantByName(
            @PathVariable
                    @NotBlank(message = "RestaurantName cant be Blank")
                    @Size(max = 255)
                    @Pattern(regexp = "^[a-zA-Z0-9 .-]+$")
                    String restaurantName);

    @Operation(
            summary = "Find restaurant by ID",
            description = "Returns a restaurant by its unique identifier",
            parameters = {
                @Parameter(
                        name = "restaurantId",
                        description = "Unique identifier of the restaurant",
                        example = "1")
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Restaurant found",
                        content = @Content(schema = @Schema(implementation = Restaurant.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Restaurant not found",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @GetMapping("/{restaurantId}")
    Mono<ResponseEntity<Restaurant>> findRestaurantById(@PathVariable Long restaurantId);

    @Operation(
            summary = "Add grade to restaurant",
            description = "Adds a new grade to an existing restaurant",
            parameters = {
                @Parameter(
                        name = "restaurantId",
                        description = "ID of the restaurant to add grade to",
                        example = "1")
            },
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Grade details",
                            required = true,
                            content =
                                    @Content(
                                            schema =
                                                    @Schema(implementation = GradesRequest.class))),
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Grade added successfully",
                        content = @Content(schema = @Schema(implementation = Restaurant.class))),
                @ApiResponse(responseCode = "400", description = "Invalid grade data"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Restaurant not found",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @PostMapping("/{restaurantId}/grade")
    Mono<ResponseEntity<Restaurant>> addGradeToRestaurant(
            @RequestBody @Valid GradesRequest request, @PathVariable("restaurantId") Long id);

    @Operation(
            summary = "Get total count of restaurants",
            description = "Returns the total number of restaurants in the system",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Total count retrieved successfully")
            })
    @GetMapping("/total")
    Mono<ResponseEntity<Long>> totalCount();

    @Operation(
            summary = "Update grades of restaurant",
            description = "Updates the grades of an existing restaurant",
            parameters = {
                @Parameter(
                        name = "restaurantId",
                        description = "ID of the restaurant to update",
                        example = "1")
            },
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Grade details",
                            required = true,
                            content =
                                    @Content(
                                            schema =
                                                    @Schema(
                                                            implementation = List.class,
                                                            subTypes = {GradesRequest.class}))),
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Grades updated successfully",
                        content = @Content(schema = @Schema(implementation = Restaurant.class))),
                @ApiResponse(responseCode = "400", description = "Invalid grade data"),
                @ApiResponse(
                        responseCode = "404",
                        description = "Restaurant not found",
                        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            })
    @PutMapping("/{restaurantId}/grades")
    Mono<ResponseEntity<Restaurant>> updateGradesOfRestaurant(
            @PathVariable Long restaurantId, @RequestBody @Valid List<GradesRequest> grades);

    @Operation(
            summary = "Create new restaurant",
            description = "Creates a new restaurant with the provided details",
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Restaurant details",
                            required = true,
                            content =
                                    @Content(
                                            schema =
                                                    @Schema(
                                                            implementation =
                                                                    RestaurantRequest.class))),
            responses = {
                @ApiResponse(responseCode = "201", description = "Restaurant created successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid restaurant data")
            })
    @PostMapping
    Mono<ResponseEntity<Void>> createRestaurant(
            @RequestBody @Valid RestaurantRequest restaurantRequest);
}
