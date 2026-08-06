package kr.ac.knue.fpe.common.domain;

import java.time.OffsetDateTime;

public record CommonRecord(String recordId, String screenId, String featureCode, String entityName, String title, String status, String payloadJson, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
