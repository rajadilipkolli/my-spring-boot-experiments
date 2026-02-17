package com.example.highrps.model.request;

import tools.jackson.databind.JsonNode;

public record EventEnvelope(String entity, JsonNode payload) {}
