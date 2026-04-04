package com.example.highrps.post.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record TagResponse(@JsonIgnore Long id, String tagName, String tagDescription) {}
