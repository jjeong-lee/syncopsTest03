package kr.ac.knue.facultyassessment.auth;

import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class MenuAuthorizationService {

    private static final List<Map.Entry<String, String>> PROTECTED_PATH_MENUS = List.of(
        Map.entry("/api/session-status", "MENU-SESSION-STATUS-MANAGEMENT"),
        Map.entry("/api/code-groups/", "MENU-DETAIL-CODE-MANAGEMENT"),
        Map.entry("/api/users/", "MENU-USER-ROLE-MANAGEMENT"),
        Map.entry("/api/users", "MENU-USER-MANAGEMENT"),
        Map.entry("/api/organizations", "MENU-ORGANIZATION-MANAGEMENT"),
        Map.entry("/api/roles", "MENU-ROLE-MANAGEMENT"),
        Map.entry("/api/menu-permissions", "MENU-MENU-PERMISSION-MANAGEMENT"),
        Map.entry("/api/menus", "MENU-MENU-STRUCTURE-MANAGEMENT"),
        Map.entry("/api/code-groups", "MENU-CODE-GROUP-MANAGEMENT")
    );

    public boolean canAccess(AuthenticationPort.AuthenticatedUser user, String requestPath) {
        String requiredMenuId = PROTECTED_PATH_MENUS.stream()
            .filter(entry -> requestPath.startsWith(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
        return requiredMenuId == null || user.menus().stream().anyMatch(menu -> requiredMenuId.equals(menu.menuId()));
    }
}
