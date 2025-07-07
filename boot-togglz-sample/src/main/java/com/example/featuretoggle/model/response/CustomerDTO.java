package com.example.featuretoggle.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerDTO {

    private Long id;
    private String name;
    private String text;
    private Integer zipCode;
}
