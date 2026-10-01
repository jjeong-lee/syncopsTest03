package kr.ac.knue.facultyassessment.auth;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class MenuAuthorizationService {

    private static final List<Map.Entry<String, String>> PROTECTED_PATH_MENUS = List.of(
        Map.entry("/api/settings/reference-years", "MENU-REFERENCE-YEAR-MANAGEMENT"),
        Map.entry("/api/settings/common", "MENU-COMMON-ENVIRONMENT-SETTINGS"),
        Map.entry("/system/settings/reference-year", "MENU-REFERENCE-YEAR-MANAGEMENT"),
        Map.entry("/system/common-codes/usage", "MENU-CODE-USAGE-MANAGEMENT"),
        Map.entry("/system/settings/common", "MENU-COMMON-ENVIRONMENT-SETTINGS"),
        Map.entry("/system/menus/usage", "MENU-MENU-USAGE-MANAGEMENT"),
        Map.entry("/system/user-organization/positions", "MENU-POSITION-ASSIGNMENT-MANAGEMENT"),
        Map.entry("/api/position-assignments", "MENU-POSITION-ASSIGNMENT-MANAGEMENT"),
        Map.entry("/api/work-assignments", "MENU-WORK-ASSIGNMENT-MANAGEMENT"),
        Map.entry("/api/role-data-scopes", "MENU-ROLE-DATA-SCOPE-MANAGEMENT"),
        Map.entry("/api/code-groups/", "MENU-DETAIL-CODE-MANAGEMENT"),
        Map.entry("/api/users/", "MENU-USER-ROLE-MANAGEMENT"),
        Map.entry("/api/users", "MENU-USER-MANAGEMENT"),
        Map.entry("/api/organizations", "MENU-ORGANIZATION-MANAGEMENT"),
        Map.entry("/api/roles", "MENU-ROLE-MANAGEMENT"),
        Map.entry("/api/menu-permissions", "MENU-MENU-PERMISSION-MANAGEMENT"),
        Map.entry("/api/menus", "MENU-MENU-STRUCTURE-MANAGEMENT"),
        Map.entry("/api/code-groups", "MENU-CODE-GROUP-MANAGEMENT")
    );

    private final JdbcTemplate jdbcTemplate;

    public MenuAuthorizationService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean canAccess(AuthenticationPort.AuthenticatedUser user, String requestPath) {
        String requiredMenuId = PROTECTED_PATH_MENUS.stream()
            .filter(entry -> requestPath.startsWith(entry.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
        return requiredMenuId == null || (
            user.menus().stream().anyMatch(menu -> requiredMenuId.equals(menu.menuId()))
                && isCurrentlyAvailable(requiredMenuId)
        );
    }

    private boolean isCurrentlyAvailable(String menuId) {
        Boolean available = jdbcTemplate.queryForObject(
            "select exists (select 1 from menu where menu_id = ? and use_yn = 'Y' "
                + "and (exposure_start_at is null or exposure_start_at <= current_timestamp) "
                + "and (exposure_end_at is null or exposure_end_at >= current_timestamp))",
            Boolean.class,
            menuId
        );
        return Boolean.TRUE.equals(available);
    }
}
