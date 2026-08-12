package kr.ac.knue.facultyassessment.auth;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import kr.ac.knue.facultyassessment.common.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class AuthenticationService {

    private final AuthenticationPort authenticationPort;

    public AuthenticationService(AuthenticationPort authenticationPort) {
        this.authenticationPort = authenticationPort;
    }

    public AuthenticationPort.LoginResult authenticate(String userId, String password, String ipAddress) {
        return authenticationPort.authenticate(userId, password, ipAddress);
    }

    public Optional<AuthenticationPort.AuthenticatedUser> findActiveSession(String sessionId) {
        return authenticationPort.findActiveSession(sessionId);
    }

    public void terminateSession(String sessionId) {
        authenticationPort.terminateSession(sessionId, "LOGOUT", null, null);
    }

    public List<AuthenticationPort.SessionSummary> findActiveSessions(AuthenticationPort.AuthenticatedUser actor) {
        requireSystemAdministrator(actor);
        return authenticationPort.findActiveSessions();
    }

    public List<AuthenticationPort.SessionEndHistory> findSessionEndHistory(AuthenticationPort.AuthenticatedUser actor,
                                                                             String userId, OffsetDateTime startedAt,
                                                                             OffsetDateTime endedAt) {
        requireSystemAdministrator(actor);
        return authenticationPort.findSessionEndHistory(userId, startedAt, endedAt);
    }

    public void forceTerminateSession(AuthenticationPort.AuthenticatedUser actor, String sessionId, String reason) {
        requireSystemAdministrator(actor);
        authenticationPort.terminateSession(sessionId, "ADMIN_FORCED", actor.userId(), reason);
    }

    private void requireSystemAdministrator(AuthenticationPort.AuthenticatedUser actor) {
        if (!actor.roleCodes().contains("R09")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "시스템관리자 권한이 필요합니다.", null);
        }
    }
}
