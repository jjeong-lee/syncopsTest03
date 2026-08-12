package kr.ac.knue.facultyassessment.auth;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AuthenticationPort {

    LoginResult authenticate(String userId, String password, String ipAddress);

    Optional<AuthenticatedUser> findActiveSession(String sessionId);

    void touchSession(String sessionId);

    void terminateSession(String sessionId, String endType, String actorUserId, String reason);

    List<SessionSummary> findActiveSessions();

    List<SessionEndHistory> findSessionEndHistory(String userId, OffsetDateTime startedAt, OffsetDateTime endedAt);

    record LoginResult(String sessionId, AuthenticatedUser user) {
    }

    record AuthenticatedUser(String userId, List<String> roleCodes, List<AuthorizedMenu> menus) {
    }

    record AuthorizedMenu(String menuId, String menuName, String parentMenuId, String route) {
    }

    record SessionSummary(String sessionId, String userId, OffsetDateTime loginAt, OffsetDateTime lastActivityAt,
                          String ipAddress, String status) {
    }

    record SessionEndHistory(String sessionId, String userId, OffsetDateTime loginAt, OffsetDateTime endedAt,
                             String endType, String actorUserId, String reason, String ipAddress) {
    }
}
