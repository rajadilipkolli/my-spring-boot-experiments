package com.example.custom.sequence.web.api;

import com.example.custom.sequence.entities.Customer;
import com.example.custom.sequence.model.response.CustomerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

public interface CustomerAPI {

    /**
     * POST /api/customers
     *
     * @param customer (required)
     * @return Created (status code 201) or Bad Request (status code 400)
     */
    @Operation(
            operationId = "createCustomer",
            tags = {"customer-controller"},
            responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Created",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CustomerResponse.class))
                        }),
                @ApiResponse(
                        responseCode = "400",
                        description = "Bad Request",
                        content = {
                            @Content(
                                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                                    schema = @Schema(implementation = ProblemDetail.class))
                        })
            })
    CustomerResponse createCustomer(
            @Valid @RequestBody(description = "", required = true) Customer customer);
}
