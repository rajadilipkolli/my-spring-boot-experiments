package com.example.ultimateredis.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ultimateredis.common.AbstractIntegrationTest;
import com.example.ultimateredis.model.Actor;
import com.example.ultimateredis.model.ActorRequest;
import com.example.ultimateredis.model.GenericResponse;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class ActorControllerTest extends AbstractIntegrationTest {

    private Actor testActor1;
    private Actor testActor2;

    @BeforeEach
    void setUp() {
        actorRepository.deleteAll();

        // Create test actors
        testActor1 = new Actor().setName("John Doe").setAge(30);
        testActor2 = new Actor().setName("Jane Smith").setAge(25);

        actorRepository.save(testActor1);
        actorRepository.save(testActor2);
    }

    @Test
    void shouldGetAllActors() {
        this.mockMvcTester
                .get()
                .uri("/api/actors")
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> {
                    @SuppressWarnings("unchecked")
                    List<LinkedHashMap<String, Object>> actors =
                            (List<LinkedHashMap<String, Object>>) response.response();
                    assertThat(actors).hasSize(2);
                    assertThat(actors)
                            .extracting(map -> map.get("name"))
                            .contains(testActor1.getName(), testActor2.getName());
                });
    }

    @Test
    void shouldGetActorById() {
        this.mockMvcTester
                .get()
                .uri("/api/actors/{id}", testActor1.getId())
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> {
                    @SuppressWarnings("unchecked")
                    var responseMap = (LinkedHashMap<String, Object>) response.response();
                    assertThat(responseMap.get("id")).isEqualTo(testActor1.getId());
                    assertThat(responseMap.get("name")).isEqualTo(testActor1.getName());
                    assertThat(responseMap.get("age")).isEqualTo(testActor1.getAge());
                });
    }

    @Test
    void shouldReturnNotFoundForNonExistingActor() {
        String nonExistingId = UUID.randomUUID().toString();

        this.mockMvcTester
                .get()
                .uri("/api/actors/{id}", nonExistingId)
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldGetActorByName() {
        this.mockMvcTester
                .get()
                .uri("/api/actors/search/by-name")
                .param("name", testActor1.getName())
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> {
                    @SuppressWarnings("unchecked")
                    var responseMap = (LinkedHashMap<String, Object>) response.response();
                    assertThat(responseMap.get("name")).isEqualTo(testActor1.getName());
                });
    }

    @Test
    void shouldReturnNotFoundForNonExistingActorByName() {
        String nonExistingName = "Non Existing Actor";

        this.mockMvcTester
                .get()
                .uri("/api/actors/search/by-name")
                .param("name", nonExistingName)
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldGetActorByNameAndAge() {
        this.mockMvcTester
                .get()
                .uri("/api/actors/search/by-name-and-age")
                .param("name", testActor1.getName())
                .param("age", String.valueOf(testActor1.getAge()))
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> {
                    @SuppressWarnings("unchecked")
                    var responseMap = (LinkedHashMap<String, Object>) response.response();
                    assertThat(responseMap.get("name")).isEqualTo(testActor1.getName());
                    assertThat(responseMap.get("age")).isEqualTo(testActor1.getAge());
                });
    }

    @Test
    void shouldCreateActor() throws Exception {
        ActorRequest newActorRequest = new ActorRequest("Robert Smith", 40);

        this.mockMvcTester
                .post()
                .uri("/api/actors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newActorRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> {
                    @SuppressWarnings("unchecked")
                    var responseMap = (LinkedHashMap<String, Object>) response.response();
                    assertThat(responseMap.get("id")).isNotNull();
                    assertThat(responseMap.get("name")).isEqualTo(newActorRequest.name());
                    assertThat(responseMap.get("age")).isEqualTo(newActorRequest.age());
                });
    }

    @Test
    void shouldFailCreateActorWithInvalidData() throws Exception {
        ActorRequest invalidActorRequest = new ActorRequest("", -1);

        this.mockMvcTester
                .post()
                .uri("/api/actors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidActorRequest))
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void shouldCreateMultipleActors() throws Exception {
        List<ActorRequest> newActorsRequest =
                Arrays.asList(new ActorRequest("Actor 1", 30), new ActorRequest("Actor 2", 35));

        this.mockMvcTester
                .post()
                .uri("/api/actors/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newActorsRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> {
                    @SuppressWarnings("unchecked")
                    List<LinkedHashMap<String, Object>> createdActors =
                            (List<LinkedHashMap<String, Object>>) response.response();
                    assertThat(createdActors).hasSize(2);
                    assertThat(createdActors).extracting(map -> map.get("name")).contains("Actor 1", "Actor 2");
                });
    }

    @Test
    void shouldFailBatchCreateWithInvalidData() throws Exception {
        List<ActorRequest> invalidActorsRequest =
                Arrays.asList(new ActorRequest("Valid Name", 30), new ActorRequest("", -5));

        this.mockMvcTester
                .post()
                .uri("/api/actors/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidActorsRequest))
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void shouldUpdateActor() throws Exception {
        ActorRequest updateRequest = new ActorRequest("Updated Name", 45);

        this.mockMvcTester
                .put()
                .uri("/api/actors/{id}", testActor1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .assertThat()
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> {
                    @SuppressWarnings("unchecked")
                    var responseMap = (LinkedHashMap<String, Object>) response.response();
                    assertThat(responseMap.get("id")).isEqualTo(testActor1.getId());
                    assertThat(responseMap.get("name")).isEqualTo(updateRequest.name());
                    assertThat(responseMap.get("age")).isEqualTo(updateRequest.age());
                });
    }

    @Test
    void shouldFailUpdateNonExistingActor() throws Exception {
        String nonExistingId = UUID.randomUUID().toString();
        ActorRequest updateRequest = new ActorRequest("Updated Name", 45);

        this.mockMvcTester
                .put()
                .uri("/api/actors/{id}", nonExistingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldFailUpdateWithInvalidData() throws Exception {
        ActorRequest invalidUpdateRequest = new ActorRequest("", -1);

        this.mockMvcTester
                .put()
                .uri("/api/actors/{id}", testActor1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdateRequest))
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void shouldDeleteActor() {
        this.mockMvcTester
                .delete()
                .uri("/api/actors/{id}", testActor1.getId())
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);

        // Verify the actor was deleted
        this.mockMvcTester
                .get()
                .uri("/api/actors/{id}", testActor1.getId())
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDeleteActorsByName() {
        // Create additional actors with the same name to test batch deletion
        Actor duplicateNameActor1 = new Actor().setName(testActor2.getName()).setAge(35);
        Actor duplicateNameActor2 = new Actor().setName(testActor2.getName()).setAge(40);
        actorRepository.save(duplicateNameActor1);
        actorRepository.save(duplicateNameActor2);

        // First verify multiple actors with the same name exist
        List<Actor> actorsWithSameName = actorRepository.findAllByName(testActor2.getName());
        assertThat(actorsWithSameName).hasSize(3); // Original + 2 new ones

        // Delete by name (should use transaction and delete all in one batch)
        this.mockMvcTester
                .delete()
                .uri("/api/actors/by-name")
                .param("name", testActor2.getName())
                .assertThat()
                .hasStatus(HttpStatus.NO_CONTENT);

        // Verify all actors with that name were deleted
        this.mockMvcTester
                .get()
                .uri("/api/actors/search/by-name")
                .param("name", testActor2.getName())
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDeleteAllActors() {

        // Delete all
        this.mockMvcTester.delete().uri("/api/actors").assertThat().hasStatus(HttpStatus.NO_CONTENT);

        // Verify all actors were deleted
        this.mockMvcTester
                .get()
                .uri("/api/actors")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> {
                    List<?> actors = (List<?>) response.response();
                    assertThat(actors).isEmpty();
                });
    }

    @Test
    void shouldReturnNotFoundForNonExistingActorByNameAndAge() {
        String nonExistingName = "Non Existing Actor";
        int nonExistingAge = 99;

        this.mockMvcTester
                .get()
                .uri("/api/actors/search/by-name-and-age")
                .param("name", nonExistingName)
                .param("age", String.valueOf(nonExistingAge))
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldFailCreateActorWithMalformedJson() {
        String malformedJson = "{\"name\":\"Broken JSON\", age:40}"; // Missing quotes around age field name

        this.mockMvcTester
                .post()
                .uri("/api/actors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson)
                .assertThat()
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingActor() {
        String nonExistingId = UUID.randomUUID().toString();

        this.mockMvcTester
                .delete()
                .uri("/api/actors/{id}", nonExistingId)
                .assertThat()
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldHandleSpecialCharactersInName() throws Exception {
        String specialName = "Jöhn Dóë with-special_characters!@#$%^&*()";
        ActorRequest specialActorRequest = new ActorRequest(specialName, 35);

        // Create actor with special characters
        this.mockMvcTester
                .post()
                .uri("/api/actors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(specialActorRequest))
                .assertThat()
                .hasStatus(HttpStatus.CREATED);

        // Search for actor with special characters by name
        this.mockMvcTester
                .get()
                .uri("/api/actors/search/by-name")
                .param("name", specialName)
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .convertTo(GenericResponse.class)
                .satisfies(response -> {
                    @SuppressWarnings("unchecked")
                    var responseMap = (LinkedHashMap<String, Object>) response.response();
                    assertThat(responseMap.get("name")).isEqualTo(specialName);
                });
    }
}
