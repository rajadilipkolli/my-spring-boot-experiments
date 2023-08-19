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
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restdocs.common.AbstractIntegrationTest;
import com.example.restdocs.entities.Gender;
import com.example.restdocs.entities.User;
import com.example.restdocs.model.request.UserRequest;
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
                .andDo(document(
                        "find-all",
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("pageNo")
                                        .description("Page you want to retrieve, 0 indexed and defaults to 0.")
                                        .optional(),
                                parameterWithName("pageSize")
                                        .description("Size of the page you want to retrieve, defaults to 10.")
                                        .optional(),
                                parameterWithName("sortBy")
                                        .description("Property name for sorting")
                                        .optional(),
                                parameterWithName("sortDir")
                                        .description("Sort direction ('asc' or 'desc')")
                                        .optional()),
                        responseFields(getPaginatedResponse())));
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
        UserRequest userRequest = new UserRequest("New User", "Last Name", 30, Gender.FEMALE, "9848022334");
        this.mockMvc
                .perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName", is(userRequest.firstName())))
                .andExpect(jsonPath("$.lastName", is(userRequest.lastName())))
                .andExpect(jsonPath("$.age", is(userRequest.age())))
                .andExpect(jsonPath("$.gender", is(userRequest.gender().name())))
                .andExpect(jsonPath("$.phoneNumber", is(userRequest.phoneNumber())))
                .andDo(document(
                        "create-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(getUserRequestFieldDescriptor()),
                        responseFields(getUserFieldDescriptor())));
    }

    @Test
    void shouldReturn400WhenCreateNewUserWithoutFirstName() throws Exception {
        UserRequest userRequest = new UserRequest(null, "Last Name", 0, Gender.MALE, "9848022334");

        this.mockMvc
                .perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
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
                .andExpect(jsonPath("$.violations[1].message", is("FirstName can't be blank")))
                .andReturn();
    }

    @Test
    void shouldUpdateUser() throws Exception {
        User user = userList.get(0);
        UserRequest userRequest = new UserRequest("Updated User", "Last Name", 50, Gender.FEMALE, "9848022334");

        this.mockMvc
                .perform(put("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.firstName", is(userRequest.firstName())))
                .andExpect(jsonPath("$.lastName", is(user.getLastName())))
                .andExpect(jsonPath("$.age", is(userRequest.age())))
                .andExpect(jsonPath("$.gender", is(userRequest.gender().name())))
                .andExpect(jsonPath("$.phoneNumber", is(user.getPhoneNumber())))
                .andDo(document(
                        "update-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("id").description("The id of the user to update")),
                        requestFields(getUserRequestFieldDescriptor()),
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

    private FieldDescriptor[] getUserRequestFieldDescriptor() {
        return new FieldDescriptor[] {
            fieldWithPath("age").description("The age of the customer").type(Integer.class.getSimpleName()),
            fieldWithPath("firstName")
                    .description("The first name of the customer")
                    .type(String.class.getSimpleName()),
            fieldWithPath("gender")
                    .description("The gender of the customer (FEMALE or MALE)")
                    .optional()
                    .type(Gender.class.getSimpleName()),
            fieldWithPath("phoneNumber")
                    .description("The cell phone number of the customer")
                    .optional()
                    .type(String.class.getSimpleName()),
            fieldWithPath("lastName")
                    .description("The last name of the customer")
                    .optional()
                    .type(String.class.getSimpleName())
        };
    }

    private FieldDescriptor[] getPaginatedResponse() {
        return new FieldDescriptor[] {
            fieldWithPath("data").description("List of user data").type(List.class.getSimpleName()),
            fieldWithPath("data[].id").description("User ID").type(Long.class.getSimpleName()),
            fieldWithPath("data[].firstName")
                    .description("First name of the user")
                    .type(String.class.getSimpleName()),
            fieldWithPath("data[].lastName")
                    .description("Last name of the user")
                    .type(String.class.getSimpleName()),
            fieldWithPath("data[].age").description("Age of the user").type(Integer.class.getSimpleName()),
            fieldWithPath("data[].gender").description("Gender of the user").type(String.class.getSimpleName()),
            fieldWithPath("data[].phoneNumber")
                    .description("Phone number of the user")
                    .type(String.class.getSimpleName()),
            fieldWithPath("totalElements").description("Total count.").type(Integer.class.getSimpleName()),
            fieldWithPath("totalPages")
                    .description("Total pages with current page size.")
                    .type(Integer.class.getSimpleName()),
            fieldWithPath("pageNumber").description("Page number.").type(Integer.class.getSimpleName()),
            fieldWithPath("isFirst")
                    .description("If this page is the first one.")
                    .type(Boolean.class.getSimpleName()),
            fieldWithPath("isLast").description("If this page is the last one.").type(Boolean.class.getSimpleName()),
            fieldWithPath("hasNext").description("Does next page exists.").type(Boolean.class.getSimpleName()),
            fieldWithPath("hasPrevious")
                    .description("Does previous page exists.")
                    .type(Boolean.class.getSimpleName())
        };
    }
}
