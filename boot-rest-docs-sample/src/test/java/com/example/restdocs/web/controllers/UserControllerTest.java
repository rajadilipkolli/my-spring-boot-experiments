package com.example.restdocs.web.controllers;

import static com.example.restdocs.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
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
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.restdocs.entities.Gender;
import com.example.restdocs.entities.User;
import com.example.restdocs.model.request.UserRequest;
import com.example.restdocs.model.response.PagedResult;
import com.example.restdocs.services.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(controllers = UserController.class)
@ActiveProfiles(PROFILE_TEST)
@AutoConfigureRestDocs
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private JsonMapper jsonMapper;

    private List<User> userList;

    @BeforeEach
    void setUp() {
        this.userList = new ArrayList<>();
        userList.add(new User()
                .setId(1L)
                .setFirstName("First User")
                .setLastName("Last Name")
                .setAge(30)
                .setGender(Gender.MALE)
                .setPhoneNumber("9848022334"));
        userList.add(new User()
                .setId(2L)
                .setFirstName("Second User")
                .setLastName("Last Name")
                .setAge(20)
                .setGender(Gender.FEMALE)
                .setPhoneNumber("9848022334"));
        userList.add(new User()
                .setId(3L)
                .setFirstName("Third User")
                .setLastName("Last Name")
                .setAge(30)
                .setGender(Gender.MALE)
                .setPhoneNumber("9848022334"));
    }

    @Test
    void shouldFetchAllUsers() throws Exception {
        Page<User> page = new PageImpl<>(userList);
        PagedResult<User> userPagedResult = new PagedResult<>(page);
        given(userService.findAllUsers(0, 10, "id", "asc")).willReturn(userPagedResult);

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
        Long userId = 1L;
        User user = new User()
                .setId(userId)
                .setFirstName("text 1")
                .setLastName("Last Name")
                .setAge(30)
                .setGender(Gender.MALE)
                .setPhoneNumber("9848022334");
        given(userService.findUserById(userId)).willReturn(Optional.of(user));

        this.mockMvc
                .perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
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
    void shouldReturn404WhenFetchingNonExistingUser() throws Exception {
        Long userId = 1L;
        given(userService.findUserById(userId)).willReturn(Optional.empty());

        this.mockMvc.perform(get("/api/users/{id}", userId)).andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewUser() throws Exception {
        User user = new User()
                .setId(34L)
                .setFirstName("some text")
                .setLastName("Last Name")
                .setAge(30)
                .setGender(Gender.MALE)
                .setPhoneNumber("9848022334");
        UserRequest userRequest = new UserRequest("some text", "Last Name", 30, Gender.MALE, "9848022334");
        given(userService.saveUser(userRequest)).willReturn(user);
        this.mockMvc
                .perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(34)))
                .andExpect(jsonPath("$.firstName", is(user.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(user.getLastName())))
                .andExpect(jsonPath("$.age", is(user.getAge())))
                .andExpect(jsonPath("$.gender", is(user.getGender().name())))
                .andExpect(jsonPath("$.phoneNumber", is(user.getPhoneNumber())))
                .andDo(document(
                        "create-user",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(getUserRequestFieldDescriptor()),
                        responseFields(getUserFieldDescriptor())));
    }

    @Test
    void shouldReturn400WhenCreateNewUserWithoutFirstNameAndAge() throws Exception {
        UserRequest userRequest = new UserRequest(null, "Last Name", 90, Gender.MALE, "9848022334");

        this.mockMvc
                .perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(userRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("https://api.restdocs.com/errors/constraint-violation")))
                .andExpect(jsonPath("$.title", is("Constraint Violation")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Invalid request content.")))
                .andExpect(jsonPath("$.instance", is("/api/users")))
                .andExpect(jsonPath("$.violations", hasSize(1)))
                .andExpect(jsonPath("$.violations[0].field", is("firstName")))
                .andExpect(jsonPath("$.violations[0].message", is("FirstName can't be blank")))
                .andReturn();
    }

    @Test
    void shouldUpdateUser() throws Exception {
        Long userId = 1L;
        User user = new User()
                .setId(userId)
                .setFirstName("some text")
                .setLastName("Last Name")
                .setAge(30)
                .setGender(Gender.MALE)
                .setPhoneNumber("9848022334");
        UserRequest userRequest = new UserRequest("some text", "Last Name", 30, Gender.MALE, "9848022334");
        given(userService.findUserById(userId)).willReturn(Optional.of(user));
        given(userService.updateUser(user, userRequest)).willReturn(user);

        this.mockMvc
                .perform(put("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
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
                        requestFields(getUserRequestFieldDescriptor()),
                        responseFields(getUserFieldDescriptor())));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingUser() throws Exception {
        Long userId = 1L;
        given(userService.findUserById(userId)).willReturn(Optional.empty());
        UserRequest userRequest = new UserRequest("Updated text", "Last Name", 30, Gender.MALE, "9848022334");

        this.mockMvc
                .perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(userRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteUser() throws Exception {
        Long userId = 1L;
        User user = new User()
                .setId(userId)
                .setFirstName("Some text")
                .setLastName("Last Name")
                .setAge(30)
                .setGender(Gender.MALE)
                .setPhoneNumber("9848022334");
        given(userService.findUserById(userId)).willReturn(Optional.of(user));
        doNothing().when(userService).deleteUserById(user.getId());

        this.mockMvc
                .perform(delete("/api/users/{id}", user.getId()))
                .andExpect(status().isOk())
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

    @Test
    void shouldReturn404WhenDeletingNonExistingUser() throws Exception {
        Long userId = 1L;
        given(userService.findUserById(userId)).willReturn(Optional.empty());

        this.mockMvc.perform(delete("/api/users/{id}", userId)).andExpect(status().isNotFound());
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
            fieldWithPath("age")
                    .description("The age of the customer")
                    .type(Integer.class.getSimpleName())
                    .attributes(key("constraints").value("Age must be greater than 0")),
            fieldWithPath("firstName")
                    .description("The first name of the customer")
                    .type(String.class.getSimpleName())
                    .attributes(key("constraints").value("Must not be null. Must not be empty")),
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
