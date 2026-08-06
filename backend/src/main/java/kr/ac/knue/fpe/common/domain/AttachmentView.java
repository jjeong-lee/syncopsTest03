package kr.ac.knue.fpe.common.domain;

import java.time.OffsetDateTime;

public record AttachmentView(String attachmentId, String businessKey, String originalFileName, long sizeBytes, String contentType, String storageKey, String malwareScanStatus, String deleteYn, OffsetDateTime createdAt) {}
