package com.example.mongoes.web.controller;

import static org.mockito.BDDMockito.given;

import com.example.mongoes.model.response.AggregationSearchResponse;
import com.example.mongoes.web.service.SearchService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(SearchController.class)
class SearchControllerTest {

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private SearchService searchService;

    @Nested
    class BoroughSearchValidation {

        @ParameterizedTest
        @ValueSource(ints = {-100, -1})
        void whenOffsetIsNegative_thenBadRequest(int offset) {
            webTestClient
                    .get()
                    .uri("/search/borough?query=test&offset=" + offset)
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenQueryIsBlank_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/borough?query=")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenLimitExceeds100_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/borough?query=test&limit=101")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenValidParameters_thenOk() {
            given(searchService.searchMatchBorough("test", 0, 10))
                    .willReturn(Mono.just(Flux.empty()));

            webTestClient
                    .get()
                    .uri("/search/borough?query=test&limit=10&offset=0")
                    .exchange()
                    .expectStatus()
                    .isOk();
        }
    }

    @Nested
    class SearchBoolMustValidation {
        @Test
        void whenValidParameters_thenReturns200() {
            given(searchService.queryBoolWithMust("Manhattan", "Italian", "Restaurant", 0, 10))
                    .willReturn(Mono.just(Flux.empty()));

            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/must/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "Restaurant")
                                            .queryParam("limit", 10)
                                            .queryParam("offset", 0)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        @Test
        void whenBoroughIsBlank_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/must/bool")
                                            .queryParam("borough", "")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "Restaurant")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenCuisineIsBlank_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/must/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "")
                                            .queryParam("name", "Restaurant")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenNameIsBlank_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/must/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenLimitExceeds100_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/must/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "Restaurant")
                                            .queryParam("limit", 101)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenLimitIsZero_thenSearchMustBoolReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/must/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "Restaurant")
                                            .queryParam("limit", 0)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenOffsetIsNegative_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/must/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "Restaurant")
                                            .queryParam("offset", -1)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }
    }

    @Nested
    class AggregateSearchValidation {
        @Test
        void whenFieldNamesIsEmpty_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/aggregate?searchKeyword=test&fieldNames=")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenSearchKeywordIsBlank_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/aggregate?searchKeyword=&fieldNames=name")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenValidSearchParameters_thenOk() {
            given(
                            searchService.aggregateSearch(
                                    "test", List.of("name"), "DESC", 10, 0, "restaurant_id"))
                    .willReturn(
                            Mono.just(
                                    new AggregationSearchResponse(
                                            List.of(), Map.of(), null, 0, 0)));

            webTestClient
                    .get()
                    .uri("/search/aggregate?searchKeyword=test&fieldNames=name&limit=10&offset=0")
                    .exchange()
                    .expectStatus()
                    .isOk();
        }
    }

    @Nested
    class WithInRangeValidation {
        @Test
        void whenLatitudeExceeds90_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/restaurant/withInRange?lat=91&lon=0&distance=10&unit=km")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenLongitudeExceeds180_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/restaurant/withInRange?lat=0&lon=181&distance=10&unit=km")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenDistanceIsNegative_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/restaurant/withInRange?lat=0&lon=0&distance=-1&unit=km")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenUnitIsInvalid_thenBadRequest() {
            webTestClient
                    .get()
                    .uri("/search/restaurant/withInRange?lat=0&lon=0&distance=10&unit=invalid")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        @DisplayName("Should return 200 OK when all parameters are valid")
        void whenAllParametersAreValid_thenOk() {
            given(searchService.searchRestaurantsWithInRange(40.7128, -74.0060, 10d, "km"))
                    .willReturn(Flux.empty());

            webTestClient
                    .get()
                    .uri(
                            "/search/restaurant/withInRange?lat=40.7128&lon=-74.0060&distance=10&unit=km")
                    .exchange()
                    .expectStatus()
                    .isOk();
        }
    }

    @Nested
    @DisplayName("Coordinate validation tests")
    class CoordinateValidationTests {

        private static final String MISSING_PARAMETER_ERROR_JSON =
                """
            {
                "type": "about:blank",
                "title": "Bad Request",
                "status": 400,
                "detail": "Required query parameter '%s' is not present.",
                "instance": "/search/restaurant/withInRange"
            }
            """;

        @ParameterizedTest
        @CsvSource({
            "-91.0, -74.0060, searchRestaurantsWithInRange.lat, -91.0, Latitude must be greater than or equal to -90",
            "91.0, -74.0060, searchRestaurantsWithInRange.lat, 91.0, Latitude must be less than or equal to 90",
            "40.7128, -181.0, searchRestaurantsWithInRange.lon, -181.0, Longitude must be greater than or equal to -180",
            "40.7128, 181.0, searchRestaurantsWithInRange.lon, 181.0, Longitude must be less than or equal to 180"
        })
        @DisplayName("Should return 400 when coordinates are out of range")
        void whenCoordinatesOutOfRange_thenReturns400(
                double lat,
                double lon,
                String field,
                double rejectedValue,
                String expectedMessage) {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/restaurant/withInRange")
                                            .queryParam("lat", lat)
                                            .queryParam("lon", lon)
                                            .queryParam("distance", 10.0)
                                            .queryParam("unit", "km")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody()
                    .jsonPath("$.type")
                    .isEqualTo("about:blank")
                    .jsonPath("$.title")
                    .isEqualTo("Constraint Violation")
                    .jsonPath("$.status")
                    .isEqualTo(400)
                    .jsonPath("$.detail")
                    .isEqualTo("Validation failed")
                    .jsonPath("$.instance")
                    .isEqualTo("/search/restaurant/withInRange")
                    .jsonPath("$.violations[0].object")
                    .isEqualTo("SearchController")
                    .jsonPath("$.violations[0].field")
                    .isEqualTo(field)
                    .jsonPath("$.violations[0].rejectedValue")
                    .isEqualTo(rejectedValue)
                    .jsonPath("$.violations[0].message")
                    .isEqualTo(expectedMessage);
        }

        @Test
        @DisplayName("Should return 400 when latitude is missing")
        void whenLatitudeMissing_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/restaurant/withInRange")
                                            .queryParam("lon", -74.0060)
                                            .queryParam("distance", 10.0)
                                            .queryParam("unit", "km")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody()
                    .json(MISSING_PARAMETER_ERROR_JSON.formatted("lat"));
        }

        @Test
        @DisplayName("Should return 400 when longitude is missing")
        void whenLongitudeMissing_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/restaurant/withInRange")
                                            .queryParam("lat", 40.7128)
                                            .queryParam("distance", 10.0)
                                            .queryParam("unit", "km")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody()
                    .json(MISSING_PARAMETER_ERROR_JSON.formatted("lon"));
        }
    }

    @Nested
    @DisplayName("Distance validation tests")
    class DistanceValidationTests {

        @Test
        @DisplayName("Should return 400 when distance is missing")
        void whenDistanceMissing_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/restaurant/withInRange")
                                            .queryParam("lat", 40.7128)
                                            .queryParam("lon", -74.0060)
                                            .queryParam("unit", "km")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody()
                    .json(
                            """
                                      {
                                          "type": "about:blank",
                                          "title": "Bad Request",
                                          "status": 400,
                                          "detail": "Required query parameter 'distance' is not present.",
                                          "instance": "/search/restaurant/withInRange"
                                      }
                                      """);
        }

        @Test
        @DisplayName("Should return 400 when distance is negative")
        void whenDistanceNegative_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/restaurant/withInRange")
                                            .queryParam("lat", 40.7128)
                                            .queryParam("lon", -74.0060)
                                            .queryParam("distance", -10.0)
                                            .queryParam("unit", "km")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody()
                    .json(
                            """
                            {
                             	"type": "about:blank",
                             	"title": "Constraint Violation",
                             	"status": 400,
                             	"detail": "Validation failed",
                             	"instance": "/search/restaurant/withInRange",
                             	"violations": [
                             		{
                             			"object": "SearchController",
                             			"field": "searchRestaurantsWithInRange.distance",
                             			"rejectedValue": -10.0,
                             			"message": "Distance must be greater than 0"
                             		}
                             	]
                            }
                            """);
        }
    }

    @Nested
    @DisplayName("Unit validation tests")
    class UnitValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"meters", "miles", "kilometers"})
        @DisplayName("Should return 400 when unit is invalid")
        void whenUnitInvalid_thenReturns400(String invalidUnit) {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/restaurant/withInRange")
                                            .queryParam("lat", 40.7128)
                                            .queryParam("lon", -74.0060)
                                            .queryParam("distance", 10.0)
                                            .queryParam("unit", invalidUnit)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody();
            //                    .jsonPath("$.message", contains("Unit must be either 'km' or
            // 'mi'"));
        }

        @Test
        @DisplayName("Should use default unit (km) when unit is not provided")
        void whenUnitNotProvided_thenUsesDefault() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/restaurant/withInRange")
                                            .queryParam("lat", 40.7128)
                                            .queryParam("lon", -74.0060)
                                            .queryParam("distance", 10.0)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isOk();
        }
    }

    @Nested
    class SearchDateRangeValidation {
        @Test
        void whenValidDateFormat_thenReturns200() {
            given(searchService.searchDateRange("2024-01-01", "2024-12-31", 0, 10))
                    .willReturn(Mono.empty());

            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/date/range")
                                            .queryParam("fromDate", "2024-01-01")
                                            .queryParam("toDate", "2024-12-31")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        @Test
        void whenInvalidFromDateFormat_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/date/range")
                                            .queryParam("fromDate", "01-01-2024") // invalid format
                                            .queryParam("toDate", "2024-12-31")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenInvalidToDateFormat_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/date/range")
                                            .queryParam("fromDate", "2024-01-01")
                                            .queryParam("toDate", "2024/12/31") // invalid format
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenMissingDates_thenReturns400() {
            webTestClient.get().uri("/search/date/range").exchange().expectStatus().isBadRequest();
        }
    }

    @Nested
    class SearchTermsValidation {

        @Test
        void whenQueriesListIsEmpty_thenBadRequest() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/terms")
                                            .queryParam("query") // empty array
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenQueriesListContainsBlankValue_thenBadRequest() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/terms")
                                            .queryParam("query", "Manhattan", " ")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenLimitExceeds100_thenBadRequest() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/terms")
                                            .queryParam("query", "Manhattan", "Brooklyn")
                                            .queryParam("limit", "101")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenOffsetIsNegative_thenBadRequest() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/terms")
                                            .queryParam("query", "Manhattan", "Brooklyn")
                                            .queryParam("offset", "-1")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenValidParameters_thenOk() {
            given(searchService.termsQueryForBorough(List.of("Manhattan", "Brooklyn"), 0, 10))
                    .willReturn(Mono.empty());

            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/terms")
                                            .queryParam("query", "Manhattan", "Brooklyn")
                                            .queryParam("limit", "10")
                                            .queryParam("offset", "0")
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isOk();
        }
    }

    @Nested
    class SearchRestaurantIdRangeValidationTests {

        @Test
        void searchRestaurantIdRange_WithValidLimits_ShouldReturnOk() {
            given(searchService.searchRestaurantIdRange(1000L, 2000L, 0, 10))
                    .willReturn(Mono.empty());
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/restaurant/range")
                                            .queryParam("lowerLimit", 1000)
                                            .queryParam("upperLimit", 2000)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        @Test
        void searchRestaurantIdRange_WithNullLowerLimit_ShouldReturnBadRequest() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/restaurant/range")
                                            .queryParam("upperLimit", 2000)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody()
                    .json(
                            """
                            {
                                "type": "about:blank",
                                "title": "Bad Request",
                                "status": 400,
                                "detail": "Required query parameter 'lowerLimit' is not present.",
                                "instance": "/search/restaurant/range"
                            }
                            """);
        }

        @Test
        void searchRestaurantIdRange_WithNullUpperLimit_ShouldReturnBadRequest() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/restaurant/range")
                                            .queryParam("lowerLimit", 1000)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody()
                    .json(
                            """
                            {
                                "type": "about:blank",
                                "title": "Bad Request",
                                "status": 400,
                                "detail": "Required query parameter 'upperLimit' is not present.",
                                "instance": "/search/restaurant/range"
                            }
                            """);
        }

        @Test
        void searchRestaurantIdRange_WithZeroLowerLimit_ShouldReturnBadRequest() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/restaurant/range")
                                            .queryParam("lowerLimit", 0)
                                            .queryParam("upperLimit", 2000)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody()
                    .json(
                            """
                            {
                            	"type": "about:blank",
                            	"title": "Constraint Violation",
                            	"status": 400,
                            	"detail": "Validation failed",
                            	"instance": "/search/restaurant/range",
                            	"violations": [
                            		{
                            			"object": "SearchController",
                            			"field": "searchRestaurantIdRange.lowerLimit",
                            			"rejectedValue": 0,
                            			"message": "Lower limit must be positive"
                            		}
                            	]
                            }
                            """);
        }

        @Test
        void searchRestaurantIdRange_WithZeroUpperLimit_ShouldReturnBadRequest() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/restaurant/range")
                                            .queryParam("lowerLimit", 1000)
                                            .queryParam("upperLimit", 0)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody()
                    .json(
                            """
                            {
                             	"type": "about:blank",
                             	"title": "Constraint Violation",
                             	"status": 400,
                             	"detail": "Validation failed",
                             	"instance": "/search/restaurant/range",
                             	"violations": [
                             		{
                             			"object": "SearchController",
                             			"field": "searchRestaurantIdRange.upperLimit",
                             			"rejectedValue": 0,
                             			"message": "Upper limit must be positive"
                             		}
                             	]
                            }
                            """);
        }

        @Test
        void searchRestaurantIdRange_WithNegativeLowerLimit_ShouldReturnBadRequest() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/restaurant/range")
                                            .queryParam("lowerLimit", -1000)
                                            .queryParam("upperLimit", 2000)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody()
                    .json(
                            """
                            {
                             	"type": "about:blank",
                             	"title": "Constraint Violation",
                             	"status": 400,
                             	"detail": "Validation failed",
                             	"instance": "/search/restaurant/range",
                             	"violations": [
                             		{
                             			"object": "SearchController",
                             			"field": "searchRestaurantIdRange.lowerLimit",
                             			"rejectedValue": -1000,
                             			"message": "Lower limit must be positive"
                             		}
                             	]
                            }
                            """);
        }

        @Test
        void searchRestaurantIdRange_WithNegativeUpperLimit_ShouldReturnBadRequest() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/restaurant/range")
                                            .queryParam("lowerLimit", 1000)
                                            .queryParam("upperLimit", -2000)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest()
                    .expectBody()
                    .json(
                            """
                            {
                             	"type": "about:blank",
                             	"title": "Constraint Violation",
                             	"status": 400,
                             	"detail": "Validation failed",
                             	"instance": "/search/restaurant/range",
                             	"violations": [
                             		{
                             			"object": "SearchController",
                             			"field": "searchRestaurantIdRange.upperLimit",
                             			"rejectedValue": -2000,
                             			"message": "Upper limit must be positive"
                             		}
                             	]
                            }
                            """);
        }
    }

    @Nested
    class SearchBoolShouldValidation {
        @Test
        void whenValidParameters_thenReturns200() {
            given(searchService.queryBoolWithShould("Manhattan", "Italian", "Restaurant", 0, 10))
                    .willReturn(Mono.empty());

            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/should/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "Restaurant")
                                            .queryParam("limit", 10)
                                            .queryParam("offset", 0)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        @Test
        void whenLimitExceeds100_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/should/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "Restaurant")
                                            .queryParam("limit", 101)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenLimitIsZero_thenSearchShouldBoolReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/should/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "Restaurant")
                                            .queryParam("limit", 0)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenOffsetIsNegative_thenReturns400() {
            webTestClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/search/should/bool")
                                            .queryParam("borough", "Manhattan")
                                            .queryParam("cuisine", "Italian")
                                            .queryParam("name", "Restaurant")
                                            .queryParam("offset", -1)
                                            .build())
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }
    }

    @Nested
    class SearchWildcardValidation {
        @Test
        void whenValidParameters_thenReturns200() {
            given(searchService.wildcardSearch("Man", 0, 10)).willReturn(Mono.empty());

            webTestClient
                    .get()
                    .uri("/search/wildcard?query=Man&limit=10&offset=0")
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        @Test
        void whenQueryIsBlank_thenReturns400() {
            webTestClient
                    .get()
                    .uri("/search/wildcard?query=&limit=10&offset=0")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenLimitExceeds100_thenReturns400() {
            webTestClient
                    .get()
                    .uri("/search/wildcard?query=Man&limit=101&offset=0")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenOffsetIsNegative_thenReturns400() {
            webTestClient
                    .get()
                    .uri("/search/wildcard?query=Man&limit=10&offset=-1")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }
    }

    @Nested
    class SearchRegularExpressionValidation {
        @Test
        void whenValidParameters_thenReturns200() {
            given(searchService.regExpSearch("Man.*", 0, 10)).willReturn(Mono.empty());

            webTestClient
                    .get()
                    .uri("/search/regexp/borough?query=Man.*&limit=10&offset=0")
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        @Test
        void whenQueryIsBlank_thenReturns400() {
            webTestClient
                    .get()
                    .uri("/search/regexp/borough?query=&limit=10&offset=0")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenLimitExceeds100_thenReturns400() {
            webTestClient
                    .get()
                    .uri("/search/regexp/borough?query=Man.*&limit=101&offset=0")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenOffsetIsNegative_thenReturns400() {
            webTestClient
                    .get()
                    .uri("/search/regexp/borough?query=Man.*&limit=10&offset=-1")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }
    }

    @Nested
    class SearchSimpleQueryForBoroughAndCuisineValidation {
        @Test
        void whenValidParameters_thenReturns200() {
            given(
                            searchService.searchSimpleQueryForBoroughAndCuisine(
                                    "Manhattan AND Italian", 0, 10))
                    .willReturn(Mono.empty());

            webTestClient
                    .get()
                    .uri("/search/simple?query=Manhattan AND Italian&limit=10&offset=0")
                    .exchange()
                    .expectStatus()
                    .isOk();
        }

        @Test
        void whenQueryIsBlank_thenReturns400() {
            webTestClient
                    .get()
                    .uri("/search/simple?query=&limit=10&offset=0")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenLimitExceeds100_thenReturns400() {
            webTestClient
                    .get()
                    .uri("/search/simple?query=Manhattan AND Italian&limit=101&offset=0")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }

        @Test
        void whenOffsetIsNegative_thenReturns400() {
            webTestClient
                    .get()
                    .uri("/search/simple?query=Manhattan AND Italian&limit=10&offset=-1")
                    .exchange()
                    .expectStatus()
                    .isBadRequest();
        }
    }
}
