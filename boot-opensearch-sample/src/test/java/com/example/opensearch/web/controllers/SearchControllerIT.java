package com.example.opensearch.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.opensearch.common.AbstractIntegrationTest;
import com.example.opensearch.entities.Address;
import com.example.opensearch.entities.Grades;
import com.example.opensearch.entities.Restaurant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.geo.Point;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchControllerIT extends AbstractIntegrationTest {

    private static final String RESTAURANT_NAME = "Lb Spumoni Gardens";
    private static final String BOROUGH_NAME = "Brooklyn";
    private static final String CUISINE_NAME = "Pizza/Italian";

    @BeforeAll
    void setUp() {
        this.restaurantRepository.deleteAll();
        Grades grade = new Grades("A", LocalDateTime.of(2022, 1, 1, 1, 1, 1), 15);
        Grades grade1 = new Grades("B", LocalDateTime.of(2022, 3, 31, 23, 59, 59), 15);
        Address addressInRange = new Address();
        addressInRange.setLocation(new Point(-73.9, 40.8));
        addressInRange.setZipcode(80986);

        // Restaurant with id in range 1-10 and location that matches 'withInRange'
        Restaurant restaurantInRange = new Restaurant();
        restaurantInRange.setId("2");
        restaurantInRange.setName(RESTAURANT_NAME);
        restaurantInRange.setBorough(BOROUGH_NAME);
        restaurantInRange.setCuisine(CUISINE_NAME);
        restaurantInRange.setAddress(addressInRange);
        restaurantInRange.setGrades(List.of(grade, grade1));

        // Restaurant with id out of range and location that does NOT match 'withInRange'
        Address addressOutOfRange = new Address();
        addressOutOfRange.setLocation(new Point(-74.5, 41.2)); // Far enough to not match
        addressOutOfRange.setZipcode(80986);

        Restaurant restaurantOutOfRange = new Restaurant();
        restaurantOutOfRange.setId("40363920");
        restaurantOutOfRange.setBorough(BOROUGH_NAME);
        restaurantOutOfRange.setCuisine("Chinese");
        restaurantOutOfRange.setName("Yono gardens");
        restaurantOutOfRange.setAddress(addressOutOfRange);
        restaurantOutOfRange.setGrades(List.of(grade, grade1));

        this.restaurantRepository.saveAll(List.of(restaurantInRange, restaurantOutOfRange));
    }

    @Test
    void searchPhrase() throws Exception {
        this.mockMvc
                .perform(get("/search/borough").param("query", BOROUGH_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.data[0].id", is("2"))) // Check the first item's "id"
                .andExpect(jsonPath("$.data[1].id", is("40363920"))); // Check the second item's "id"
    }

    @Test
    void searchMulti() throws Exception {
        this.mockMvc
                .perform(get("/search/multi").param("query", BOROUGH_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.data[0].content.id", is("2"))) // Check the first item's "id"
                .andExpect(jsonPath("$.data[1].content.id", is("40363920"))); // Check the second item's "id"
    }

    @Test
    void searchTermForBorough() throws Exception {
        this.mockMvc
                .perform(get("/search/term/borough").param("query", BOROUGH_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.data[0].content.id", is("2"))) // Check the first item's "id"
                .andExpect(jsonPath("$.data[1].content.id", is("40363920"))); // Check the second item's "id"
    }

    @Test
    void searchTerms() throws Exception {
        this.mockMvc
                .perform(get("/search/terms").param("query", BOROUGH_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.data[0].content.id", is("2"))) // Check the first item's "id"
                .andExpect(jsonPath("$.data[1].content.id", is("40363920"))); // Check the second item's "id"
    }

    @Test
    void queryBoolWithMust() throws Exception {
        this.mockMvc
                .perform(get("/search/must/bool")
                        .param("borough", BOROUGH_NAME)
                        .param("cuisine", CUISINE_NAME)
                        .param("name", RESTAURANT_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(1)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.data[0].id", is("2"))); // Check the first item's "id"
    }

    @Test
    void searchBoolShould() throws Exception {
        this.mockMvc
                .perform(get("/search/should/bool")
                        .param("borough", BOROUGH_NAME)
                        .param("cuisine", CUISINE_NAME)
                        .param("name", RESTAURANT_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.data[0].content.id", is("2"))) // Check the first item's "id"
                .andExpect(jsonPath("$.data[1].content.id", is("40363920"))); // Check the second item's "id"
    }

    @Test
    void searchWildCardBoroughFail() throws Exception {
        this.mockMvc
                .perform(get("/search/wildcard/borough").param("query", "Spumoni"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(0)))
                .andExpect(jsonPath("$.totalElements", is(0)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(0)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void searchWildCardBorough() throws Exception {
        this.mockMvc
                .perform(get("/search/wildcard/borough").param("query", "ines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(1)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.data[0].content.id", is("40363920")));
    }

    @Test
    void searchRegularExpression() throws Exception {
        this.mockMvc
                .perform(get("/search/regexp/borough").param("query", "B.[a-z]*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.data[0].content.id", is("2"))) // Check the first item's "id"
                .andExpect(jsonPath("$.data[1].content.id", is("40363920"))); // Check the second item's "id"
    }

    @Test
    void searchSimpleQueryForBoroughAndCuisine() throws Exception {
        this.mockMvc
                .perform(get("/search/simple").param("query", BOROUGH_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.data[0].content.id", is("2"))) // Check the first item's "id"
                .andExpect(jsonPath("$.data[1].content.id", is("40363920"))); // Check the second item's "id"
    }

    @Test
    void searchRestaurantIdRange() throws Exception {
        this.mockMvc
                .perform(
                        get("/search/restaurant/range").param("lowerLimit", "1").param("upperLimit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(1)))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.data[0].content.id", is("2"))); // Check the first item's "id"
    }

    @Test
    void searchDateRange() throws Exception {
        this.mockMvc
                .perform(get("/search/date/range")
                        .param(
                                "fromDate",
                                LocalDateTime.of(2021, 12, 31, 23, 59, 59).toString())
                        .param("toDate", LocalDateTime.of(2022, 4, 11, 0, 0, 0).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andExpect(jsonPath("$.data[0].content.id", is("2"))) // Check the first item's "id"
                .andExpect(jsonPath("$.data[1].content.id", is("40363920"))); // Check the second item's "id"
    }

    @Test
    void aggregateSearch() throws Exception {
        this.mockMvc
                .perform(get("/search/aggregate")
                        .param("searchKeyword", "Pizza")
                        .param("fieldNames", "name", "borough", "cuisine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aggregationMap.length()").value(3))
                .andExpect(jsonPath("$.aggregationMap.MyBorough.brooklyn").value(1))
                .andExpect(jsonPath("$.aggregationMap.MyCuisine.italian").value(1))
                .andExpect(jsonPath("$.aggregationMap.MyCuisine.pizza").value(1))
                .andExpect(jsonPath("$.aggregationMap.MyDateRange.length()").value(1))
                .andExpect(jsonPath("$.data.length()", is(1))) // Check the number of items in "content"
                .andExpect(jsonPath("$.totalElements", is(1))) // Check the total number of elements
                .andExpect(jsonPath("$.totalPages", is(1))) // Check the total number of pages
                .andExpect(jsonPath("$.pageNumber", is(1))) // Check the current page number
                .andExpect(jsonPath("$.isFirst", is(true))) // Check if it's the first page
                .andExpect(jsonPath("$.isLast", is(true))) // Check if it's the last page
                .andExpect(jsonPath("$.hasNext", is(false))) // Check if there is a next page
                .andExpect(jsonPath("$.hasPrevious", is(false))) // Check if there is a previous page
                .andExpect(jsonPath("$.data[0].content.id", is("2"))); // Check the first item's "id"
    }

    @Test
    void searchRestaurantsWithInRange() throws Exception {
        this.mockMvc
                .perform(get("/search/restaurant/withInRange")
                        .param("lat", "40.75")
                        .param("lon", "-73.9")
                        .param("distance", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].name", is("Lb Spumoni Gardens")))
                .andExpect(jsonPath("$[0].dist", is(5.559751998519038)))
                .andExpect(jsonPath("$[0].location.x", is(closeTo(-73.9, 0.001))))
                .andExpect(jsonPath("$[0].location.y", is(closeTo(40.8, 0.001))));
    }
}
