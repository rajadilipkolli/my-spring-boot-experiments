package com.example.mongoes.web.api;

import com.example.mongoes.elasticsearch.domain.ERestaurant;
import com.example.mongoes.mongodb.domain.Notes;
import com.example.mongoes.mongodb.domain.Restaurant;
import com.example.mongoes.web.response.GenericMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface RestaurantApi {

    @Operation(
            summary = "finds Restaurant by Name",
            description = "returns Restaurant with given Name")
    Mono<ResponseEntity<ERestaurant>> findRestaurantByName(
             String restaurantName);

    @ApiResponses(
        value = {
        @ApiResponse(
            responseCode = "201",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema =
                        @Schema(
                            implementation = GenericMessage.class,
                            example = """
                                    {
                                        "message": "restaurant with name xyz created"
                                    }"""))),
        @ApiResponse(
            responseCode = "400",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                    ),
            description = "Please check request body")
        })
    @Operation(
            summary = "creates Restaurant after validation",
            description = "validates the data and the created Restaurant")
    ResponseEntity<Object> createRestaurant(Restaurant restaurant);

    @Operation(
        summary = "adds Notes to the given Restaurant",
        description = "validates if restaurant exists and then adds the notes to the repository")
    @ApiResponses(
        value = {
        @ApiResponse(
            responseCode = "404",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GenericMessage.class)),
            description = "Restaurant Not Found to Update")
        })
    Mono<ResponseEntity<Restaurant>> addNotesToRestaurant(String restaurantName, Notes notes);
}
