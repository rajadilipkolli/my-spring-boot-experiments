package com.example.restdocs.web.controllers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restdocs.common.AbstractIntegrationTest;
import com.example.restdocs.entities.Gender;
import com.example.restdocs.entities.User;
import com.example.restdocs.repositories.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;

class UserControllerIT extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private List<User> userList = null;

    @BeforeEach
    void setUp() {
        userRepository.deleteAllInBatch();

        userList = new ArrayList<>();
        userList.add(new User(null, "First User", "Last Name", 30, Gender.MALE, "9848022334"));
        userList.add(new User(null, "Second User", "Last Name", 20, Gender.FEMALE, "9848022334"));
        userList.add(new User(null, "Third User", "Last Name", 30, Gender.MALE, "9848022334"));
        userList = userRepository.saveAll(userList);
    }

    @Test
    void shouldFetchAllUsers() throws Exception {
        this.mockMvc
                .perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(userList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)))
                .andDo(document("find-all", preprocessResponse(prettyPrint())));
    }

    @Test
    void shouldFindUserById() throws Exception {
        User user = userList.get(0);
        Long userId = user.getId();

        this.mockMvc
                .perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(user.getLastName())))
                .andExpect(jsonPath("$.age", is(user.getAge())))
                .andExpect(jsonPath("$.gender", is(user.getGender().name())))
                .andExpect(jsonPath("$.phoneNumber", is(user.getPhoneNumber())))
                .andDo(document(
                        "find-by-id",
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("id").description("The id of the user to retrieve")),
                        responseFields(getUserFieldDescriptor())));
    }

    @Test
    void shouldCreateNewUser() throws Exception {
        User user = new User(null, "New User", "Last Name", 30, Gender.FEMALE, "9848022334");
        this.mockMvc
                .perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(user.getLastName())))
                .andExpect(jsonPath("$.age", is(user.getAge())))
                .andExpect(jsonPath("$.gender", is(user.getGender().name())))
                .andExpect(jsonPath("$.phoneNumber", is(user.getPhoneNumber())))
                .andDo(document(
                        "create-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(getUserFieldDescriptor()),
                        responseFields(getUserFieldDescriptor())));
    }

    @Test
    void shouldReturn400WhenCreateNewUserWithoutText() throws Exception {
        User user = new User(null, null, "Last Name", 0, Gender.MALE, "9848022334");

        this.mockMvc
                .perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is("application/problem+json")))
                .andExpect(jsonPath("$.type", is("about:blank")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/users")))
                .andExpect(jsonPath("$.violations", hasSize(2)))
                .andExpect(jsonPath("$.violations[0].field", is("age")))
                .andExpect(jsonPath("$.violations[0].message", is("Age must be greater than 0")))
                .andExpect(jsonPath("$.violations[1].field", is("firstName")))
                .andExpect(jsonPath("$.violations[1].message", is("FirstName can't be empty")))
                .andReturn();
    }

    @Test
    void shouldUpdateUser() throws Exception {
        User user = userList.get(0);
        user.setFirstName("Updated User");

        this.mockMvc
                .perform(put("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(user.getLastName())))
                .andExpect(jsonPath("$.age", is(user.getAge())))
                .andExpect(jsonPath("$.gender", is(user.getGender().name())))
                .andExpect(jsonPath("$.phoneNumber", is(user.getPhoneNumber())))
                .andDo(document(
                        "update-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("id").description("The id of the user to update")),
                        responseFields(getUserFieldDescriptor())));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        User user = userList.get(0);

        this.mockMvc
                .perform(delete("/api/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(user.getLastName())))
                .andExpect(jsonPath("$.age", is(user.getAge())))
                .andExpect(jsonPath("$.gender", is(user.getGender().name())))
                .andExpect(jsonPath("$.phoneNumber", is(user.getPhoneNumber())))
                .andDo(document(
                        "delete-user",
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("id").description("The id of the user to delete")),
                        responseFields(getUserFieldDescriptor())));
    }

    private FieldDescriptor[] getUserFieldDescriptor() {
        return new FieldDescriptor[] {
            fieldWithPath("age").description("The age of the customer").type(Integer.class.getSimpleName()),
            fieldWithPath("firstName")
                    .description("The first name of the customer")
                    .type(String.class.getSimpleName()),
            fieldWithPath("gender")
                    .description("The gender of the customer (FEMALE or MALE)")
                    .type(Gender.class.getSimpleName()),
            fieldWithPath("phoneNumber")
                    .description("The cell phone number of the customer")
                    .type(String.class.getSimpleName()),
            fieldWithPath("id")
                    .description("The unique id of the customer")
                    .optional()
                    .type(Long.class.getSimpleName()),
            fieldWithPath("lastName")
                    .description("The last name of the customer")
                    .optional()
                    .type(String.class.getSimpleName())
        };
    }
}
