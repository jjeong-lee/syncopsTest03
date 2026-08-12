package kr.ac.knue.facultyassessment.sessionstatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import kr.ac.knue.facultyassessment.common.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class SessionStatusService {
    private final SessionStatusMapper mapper;

    public SessionStatusService(SessionStatusMapper mapper) {
        this.mapper = mapper;
    }

    public List<SessionStatus> findActiveSessions(int page, int size) {
        return mapper.findActiveSessions(size, page * size);
    }

    public List<SessionHistory> findTerminationHistory(String userId, OffsetDateTime startedAt, OffsetDateTime endedAt, int page, int size) {
        if (startedAt != null && endedAt != null && startedAt.isAfter(endedAt)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PERIOD", "시작일시는 종료일시보다 늦을 수 없습니다.", "startedAt");
        }
        return mapper.findTerminationHistory(userId, startedAt, endedAt, size, page * size);
    }

    public SessionHistory findTerminationHistoryById(String sessionId) {
        SessionHistory history = mapper.findTerminationHistoryById(sessionId);
        if (history == null) throw new ApiException(HttpStatus.NOT_FOUND, "SESSION_HISTORY_NOT_FOUND", "종료이력을 찾을 수 없습니다.", "sessionId");
        return history;
    }

    @Transactional
    public void terminate(String sessionId, String reason, String actorUserId) {
        if (reason == null || reason.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "강제종료 사유를 입력하세요.", "reason");
        }
        String status = mapper.findStatus(sessionId);
        if (status == null) throw new ApiException(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "세션을 찾을 수 없습니다.", "sessionId");
        if (!"ACTIVE".equals(status)) throw new ApiException(HttpStatus.CONFLICT, "SESSION_ALREADY_TERMINATED", "이미 종료된 세션입니다.", "sessionId");
        if (mapper.terminateActiveSession(sessionId, actorUserId, reason.trim()) != 1) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_ALREADY_TERMINATED", "이미 종료된 세션입니다.", "sessionId");
        }
        mapper.insertTerminationAudit("CHANGE-" + UUID.randomUUID(), sessionId, actorUserId, reason.trim());
    }

    public TerminationAudit findTerminationAudit(String sessionId) {
        TerminationAudit audit = mapper.findTerminationAudit(sessionId);
        if (audit == null) throw new ApiException(HttpStatus.NOT_FOUND, "TERMINATION_AUDIT_NOT_FOUND", "강제종료 감사기록을 찾을 수 없습니다.", "sessionId");
        return audit;
    }

    public record SessionStatus(String sessionId, String userId, OffsetDateTime loginAt, OffsetDateTime lastActivityAt, String ipAddress, String status) {}
    public record SessionHistory(String sessionId, String userId, OffsetDateTime loginAt, OffsetDateTime lastActivityAt, String ipAddress, String status, String terminationType, OffsetDateTime terminatedAt, String terminatedBy, String terminationReason) {}
    public record TerminationAudit(String changeHistoryId, String actorUserId, String reason, OffsetDateTime changedAt) {}
}
