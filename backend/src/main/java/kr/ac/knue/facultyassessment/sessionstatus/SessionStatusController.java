package kr.ac.knue.facultyassessment.sessionstatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import kr.ac.knue.facultyassessment.auth.AuthenticationPort;
import kr.ac.knue.facultyassessment.auth.SessionAuthorizationFilter;
import kr.ac.knue.facultyassessment.common.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class SessionStatusController {
    private final SessionStatusService service;
    public SessionStatusController(SessionStatusService service) { this.service = service; }

    @GetMapping("/api/session-status")
    public ApiResponse<?> listSessionStatus(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.findActiveSessions(validPage(page), validSize(size)));
    }

    @PostMapping("/api/session-status/{sessionId}/termination")
    public ApiResponse<Void> updateSessionTermination(@PathVariable String sessionId, @Valid @RequestBody SessionTerminationRequest request, HttpServletRequest httpRequest) {
        AuthenticationPort.AuthenticatedUser actor = actor(httpRequest);
        service.terminate(sessionId, request.reason(), actor.userId());
        return ApiResponse.success(null);
    }

    @GetMapping("/api/session-termination-history")
    public ApiResponse<?> listSessionTerminationHistory(@RequestParam(required = false) String userId, @RequestParam(required = false) OffsetDateTime startedAt,
        @RequestParam(required = false) OffsetDateTime endedAt, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.findTerminationHistory(userId, startedAt, endedAt, validPage(page), validSize(size)));
    }

    @GetMapping("/api/session-termination-history/{sessionId}")
    public ApiResponse<?> getSessionTerminationHistory(@PathVariable String sessionId) { return ApiResponse.success(service.findTerminationHistoryById(sessionId)); }

    @GetMapping("/api/session-status/{sessionId}/termination/audit")
    public ApiResponse<?> getSessionTerminationAudit(@PathVariable String sessionId) { return ApiResponse.success(service.findTerminationAudit(sessionId)); }

    private AuthenticationPort.AuthenticatedUser actor(HttpServletRequest request) { return (AuthenticationPort.AuthenticatedUser) request.getAttribute(SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE); }
    private int validPage(int page) { return Math.max(page, 0); }
    private int validSize(int size) { return Math.max(1, size); }
    public record SessionTerminationRequest(@NotBlank(message = "강제종료 사유를 입력하세요.") String reason) {}
}
