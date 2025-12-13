package com.example.highrps;


import com.example.highrps.repository.EventDto;
import tools.jackson.databind.ObjectMapper;

public record StatsResponse(String id, Long value) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public StatsResponse(EventDto eventDto) {
        this(eventDto.getId(), eventDto.getValue());
    }

    public static StatsResponse empty(String id) {
        return new StatsResponse(id, 0L);
    }

    public static StatsResponse fromJson(String json) {
        try {
            return MAPPER.readValue(json, StatsResponse.class);
        } catch (Exception e) {
            return new StatsResponse("unknown", 0L);
        }
    }

    public static String toJson(StatsResponse statsResponse) {
        try {
            return MAPPER.writeValueAsString(statsResponse);
        } catch (Exception e) {
            return "{}";
        }
    }
}
