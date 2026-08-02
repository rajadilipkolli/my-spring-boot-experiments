package com.example.highrps.shared;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base interface for all domain events to enforce schema versioning
 * and backward/forward compatibility rules.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public interface DomainEvent {

    /**
     * The schema version for this event, used by consumers to determine
     * how to deserialize or process the payload. Defaults to 1.0.
     */
    @JsonProperty("schemaVersion")
    default String schemaVersion() {
        return "1.0";
    }
}
