package com.example.ultimatepostgres.model;

import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public class JobRequest {

    @NotNull
    private JsonNode payload;

    private Integer priority = 0;

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
