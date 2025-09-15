package com.hawkevent.model;

import com.hawkevent.enums.ActionTakenEnum;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Event {
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

}
