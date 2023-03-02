package com.example.rest.template.model.response;

import java.util.Map;

public record ApplicationRestResponse<T>(T body, int statusCode, Map<String, String> headers) {}
