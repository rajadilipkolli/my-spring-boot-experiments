package com.example.mongoes.web.api;

import com.example.mongoes.document.Restaurant;
import com.example.mongoes.model.response.ResultData;
import com.example.mongoes.model.response.SearchPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Search", description = "Restaurant search operations API")
public interface SearchApi {

    @Operation(
            summary = "Search restaurants by borough using phrase match",
            description = "Search for restaurants in a specific borough using phrase matching",
            parameters = {
                @Parameter(
                        name = "query",
                        in = ParameterIn.QUERY,
                        description = "Borough name to search for",
                        example = "Manhattan"),
                @Parameter(
                        name = "limit",
                        in = ParameterIn.QUERY,
                        description = "Maximum number of results (1-100) per page",
                        example = "10"),
                @Parameter(
                        name = "offset",
                        in = ParameterIn.QUERY,
                        description = "Starting position in pagination",
                        example = "0")
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved restaurants",
                        content = @Content(schema = @Schema(implementation = Restaurant.class))),
                @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
            })
    @GetMapping("/search/borough")
    Mono<ResponseEntity<Flux<Restaurant>>> searchPhrase(
            @RequestParam @NotBlank(message = "Query cannot be blank") String query,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset);

    @Operation(
            summary = "Multi-field search for restaurants",
            description = "Search across multiple fields with optional prefix phrase matching",
            parameters = {
                @Parameter(
                        name = "query",
                        in = ParameterIn.QUERY,
                        description = "Search query across multiple fields",
                        example = "Italian Manhattan"),
                @Parameter(
                        name = "limit",
                        in = ParameterIn.QUERY,
                        description = "Maximum number of results (1-100) per page",
                        example = "10"),
                @Parameter(
                        name = "offset",
                        in = ParameterIn.QUERY,
                        description = "Starting position in pagination",
                        example = "0"),
                @Parameter(
                        in = ParameterIn.QUERY,
                        description = "Enable prefix phrase matching",
                        example = "false")
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved restaurants",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                SearchPageResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
            })
    @GetMapping("/search/multi")
    Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchMulti(
            @RequestParam @NotBlank(message = "Query cannot be blank") String query,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset,
            @RequestParam(value = "prefix_phrase_enabled", defaultValue = "false")
                    Boolean prefixPhraseEnabled);

    @Operation(
            summary = "Term search for borough",
            description = "Search for restaurants using exact borough term match",
            parameters = {
                @Parameter(
                        name = "query",
                        in = ParameterIn.QUERY,
                        description = "search keyword",
                        example = "Manhattan"),
                @Parameter(
                        name = "limit",
                        in = ParameterIn.QUERY,
                        description = "Maximum number of results (1-100) per page",
                        example = "10"),
                @Parameter(
                        name = "offset",
                        in = ParameterIn.QUERY,
                        description = "Starting position in pagination",
                        example = "0")
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved restaurants",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                SearchPageResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
            })
    @GetMapping("/search/term/borough")
    Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchTermForBorough(
            @RequestParam @NotBlank(message = "Query cannot be blank") String query,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset);

    @Operation(
            summary = "Terms search for boroughs",
            description = "Search for restaurants in multiple boroughs using terms matching",
            parameters = {
                @Parameter(
                        name = "query",
                        in = ParameterIn.QUERY,
                        description = "List of borough names to search for",
                        example = "['Manhattan', 'Brooklyn']"),
                @Parameter(
                        name = "limit",
                        in = ParameterIn.QUERY,
                        description = "Maximum number of results (1-100) per page",
                        example = "10"),
                @Parameter(
                        name = "offset",
                        in = ParameterIn.QUERY,
                        description = "Starting position in pagination",
                        example = "0")
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved restaurants",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                SearchPageResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
            })
    @GetMapping("/search/terms")
    Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchTerms(
            @RequestParam("query") @NotEmpty(message = "Queries list cannot be empty") @Valid
                    List<@NotBlank(message = "Query term cannot be blank") String> queries,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset);

    @Operation(
            summary = "Boolean must search",
            description = "Search for restaurants matching all specified criteria (AND condition)",
            parameters = {
                @Parameter(
                        name = "borough",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "Borough name (required)",
                        example = "Manhattan"),
                @Parameter(
                        name = "cuisine",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "Cuisine type (required)",
                        example = "Italian"),
                @Parameter(
                        name = "name",
                        in = ParameterIn.QUERY,
                        required = true,
                        description = "Restaurant name (required)",
                        example = "Pizza"),
                @Parameter(
                        name = "limit",
                        in = ParameterIn.QUERY,
                        description = "Maximum number of results (1-100) per page",
                        example = "10"),
                @Parameter(
                        name = "offset",
                        in = ParameterIn.QUERY,
                        description = "Starting position in pagination",
                        example = "0")
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved restaurants",
                        content = @Content(schema = @Schema(implementation = Restaurant.class))),
                @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
            })
    @GetMapping("/search/must/bool")
    Mono<ResponseEntity<Flux<Restaurant>>> searchBoolMust(
            @RequestParam @NotBlank(message = "Borough cannot be blank") String borough,
            @RequestParam @NotBlank(message = "Cuisine cannot be blank") String cuisine,
            @RequestParam @NotBlank(message = "Name cannot be blank") String name,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset);

    @Operation(
            summary = "Boolean should search",
            description =
                    "Search for restaurants matching any of the specified criteria (OR condition)",
            parameters = {
                @Parameter(name = "borough", description = "Borough name", example = "Manhattan"),
                @Parameter(name = "cuisine", description = "Cuisine type", example = "Italian"),
                @Parameter(name = "name", description = "Restaurant name", example = "Pizza"),
                @Parameter(
                        name = "limit",
                        description = "Maximum number of results (1-100)",
                        example = "10"),
                @Parameter(
                        name = "offset",
                        description = "Number of results to skip",
                        example = "0")
            },
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved restaurants",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                SearchPageResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
            })
    @GetMapping("/search/should/bool")
    Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchBoolShould(
            @RequestParam String borough,
            @RequestParam String cuisine,
            @RequestParam String name,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset);

    @Operation(
            summary = "Wildcard borough search",
            description = "Search for restaurants using wildcard pattern matching on borough names",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved restaurants",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                SearchPageResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
            })
    @GetMapping("/search/wildcard")
    Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchWildcard(
            @Parameter(description = "Wildcard pattern for search", example = "Man")
                    @RequestParam
                    @NotBlank(message = "Query cannot be blank")
                    String query,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @RequestParam(defaultValue = "0") @Min(0) Integer offset);

    @Operation(
            summary = "Regular expression borough search",
            description =
                    "Search for restaurants using regular expression pattern matching on borough names",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved restaurants",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                SearchPageResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
            })
    @GetMapping("/search/regexp/borough")
    Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchRegularExpression(
            @Parameter(description = "Regular expression pattern", example = "Man.*")
                    @RequestParam
                    @NotBlank(message = "Query cannot be blank")
                    String query,
            @Parameter(description = "Maximum number of results (1-100)", example = "10")
                    @RequestParam(defaultValue = "10")
                    @Min(1)
                    @Max(100)
                    Integer limit,
            @Parameter(description = "Number of results to skip", example = "0")
                    @RequestParam(defaultValue = "0")
                    @Min(0)
                    Integer offset);

    @Operation(
            summary = "Simple query search",
            description =
                    "Search for restaurants using a simple query string across borough and cuisine",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved restaurants",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                SearchPageResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
            })
    @GetMapping("/search/simple")
    Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchSimpleQueryForBoroughAndCuisine(
            @Parameter(description = "Search query string", example = "Manhattan AND Italian")
                    @RequestParam
                    @NotBlank(message = "Query cannot be blank")
                    String query,
            @Parameter(description = "Maximum number of results (1-100)", example = "10")
                    @RequestParam(defaultValue = "10")
                    @Min(1)
                    @Max(100)
                    Integer limit,
            @Parameter(description = "Number of results to skip", example = "0")
                    @RequestParam(defaultValue = "0")
                    @Min(0)
                    Integer offset);

    @Operation(
            summary = "Restaurant ID range search",
            description = "Search for restaurants within a specified ID range",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved restaurants",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                SearchPageResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
            })
    @GetMapping("/search/restaurant/range")
    Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchRestaurantIdRange(
            @Parameter(description = "Lower limit of restaurant ID", example = "1000")
                    @RequestParam
                    @NotNull(message = "Lower limit is required")
                    @Positive(message = "Lower limit must be positive")
                    Long lowerLimit,
            @Parameter(description = "Upper limit of restaurant ID", example = "2000")
                    @RequestParam
                    @Positive(message = "Upper limit must be positive")
                    @NotNull(message = "Upper limit is required")
                    Long upperLimit,
            @Parameter(description = "Maximum number of results (1-100)", example = "10")
                    @RequestParam(defaultValue = "10")
                    @Min(1)
                    @Max(100)
                    Integer limit,
            @Parameter(description = "Number of results to skip", example = "0")
                    @RequestParam(defaultValue = "0")
                    @Min(0)
                    Integer offset);

    @Operation(
            summary = "Date range search",
            description = "Search for restaurants within a specified date range",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved restaurants",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                SearchPageResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
            })
    @GetMapping("/search/date/range")
    Mono<ResponseEntity<SearchPageResponse<Restaurant>>> searchDateRange(
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01")
                    @RequestParam
                    @NotBlank(message = "From date is required")
                    @Pattern(
                            regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                            message = "Date must be in yyyy-MM-dd format")
                    String fromDate,
            @Parameter(description = "End date (ISO format)", example = "2024-12-31")
                    @RequestParam
                    @NotBlank(message = "To date is required")
                    @Pattern(
                            regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                            message = "Date must be in yyyy-MM-dd format")
                    String toDate,
            @Parameter(description = "Maximum number of results (1-100)", example = "10")
                    @RequestParam(defaultValue = "10")
                    @Min(1)
                    @Max(100)
                    Integer limit,
            @Parameter(description = "Number of results to skip", example = "0")
                    @RequestParam(defaultValue = "0")
                    @Min(0)
                    Integer offset);

    @Operation(
            summary = "Aggregation search",
            description = "Search and aggregate results based on specified fields",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved aggregated results",
                        content =
                                @Content(
                                        schema =
                                                @Schema(
                                                        implementation =
                                                                SearchPageResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
            })
    @GetMapping("/search/aggregate")
    Mono<ResponseEntity<SearchPageResponse<Restaurant>>> aggregateSearch(
            @Parameter(description = "Search keyword", example = "Italian")
                    @RequestParam
                    @NotBlank(message = "Search keyword cannot be blank")
                    String searchKeyword,
            @Parameter(description = "Fields to aggregate on", example = "['borough', 'cuisine']")
                    @RequestParam
                    @NotEmpty(message = "Field names cannot be empty")
                    List<String> fieldNames,
            @Parameter(description = "Maximum number of results (1-100)", example = "15")
                    @RequestParam(required = false, defaultValue = "15")
                    @Min(1)
                    @Max(100)
                    Integer limit,
            @Parameter(description = "Number of results to skip", example = "0")
                    @RequestParam(required = false, defaultValue = "0")
                    @Min(0)
                    Integer offset,
            @Parameter(description = "Sort order (ASC or DESC)", example = "DESC")
                    @RequestParam(required = false, defaultValue = "DESC")
                    String sortOrder,
            @Parameter(description = "Fields to sort by", example = "restaurant_id")
                    @RequestParam(required = false, defaultValue = "restaurant_id")
                    String... sortFields);

    @Operation(
            summary = "Search restaurants within range",
            description = "Find restaurants within specified distance from given coordinates",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved restaurants",
                        content = @Content(schema = @Schema(implementation = ResultData.class))),
                @ApiResponse(responseCode = "400", description = "Invalid parameters provided")
            })
    @GetMapping("/search/restaurant/withInRange")
    Flux<ResultData> searchRestaurantsWithInRange(
            @Parameter(
                            description = "Latitude coordinate (between -90 and 90)",
                            example = "40.7128")
                    @RequestParam
                    @Min(value = -90, message = "Latitude must be greater than or equal to -90")
                    @Max(value = 90, message = "Latitude must be less than or equal to 90")
                    @NotNull(message = "Latitude is required")
                    Double lat,
            @Parameter(
                            description = "Longitude coordinate (between -180 and 180)",
                            example = "-74.0060")
                    @RequestParam
                    @Min(value = -180, message = "Longitude must be greater than or equal to -180")
                    @Max(value = 180, message = "Longitude must be less than or equal to 180")
                    @NotNull(message = "Longitude is required")
                    Double lon,
            @Parameter(description = "Distance from coordinates (must be positive)")
                    @RequestParam
                    @Positive(message = "Distance must be greater than 0")
                    @Max(value = 20000, message = "Distance cannot exceed 20000")
                    @NotNull(message = "Distance is required")
                    Double distance,
            @Parameter(
                            description = "Unit of distance",
                            example = "km",
                            schema = @Schema(allowableValues = {"km", "mi"}))
                    @RequestParam(defaultValue = "km", required = false)
                    @Pattern(regexp = "^(km|mi)$", message = "Unit must be either 'km' or 'mi'")
                    @NotBlank(message = "Unit cannot be blank")
                    String unit);
}
