package com.example.highrps.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record TagResponse(@JsonIgnore Long id, String tagName, String tagDescription) {}
