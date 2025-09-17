package com.hawkevent.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hawkevent.enums.ActionTakenEnum;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventModel {
    UUID eventId;
    String traceId;
    Instant timestamp;
    String sourceIp;
    int sourcePort;
    String method;
    String path;
    Map<String, String> queryParams;
    Map<String, String> headers;
    String body;
    String bodyHash;
    double suspicionScore;
    List<String> detectedRules;
    ActionTakenEnum actionTaken;
    boolean bodyTruncated;
    Map<String, Object> enrichment;

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.writeValueAsString(this);
    }

}
