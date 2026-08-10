package kr.ac.knue.facultyassessment.auth;

import java.util.List;
import java.util.Optional;

public interface AuthenticationPort {

    LoginResult authenticate(String userId, String password);

    Optional<AuthenticatedUser> findActiveSession(String sessionId);

    void terminateSession(String sessionId);

    record LoginResult(String sessionId, AuthenticatedUser user) {
    }

    record AuthenticatedUser(String userId, List<String> roleCodes, List<AuthorizedMenu> menus) {
    }

    record AuthorizedMenu(String menuId, String menuName, String parentMenuId, String route) {
    }
}
