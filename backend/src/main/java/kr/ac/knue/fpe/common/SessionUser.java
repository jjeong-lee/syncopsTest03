package kr.ac.knue.fpe.common;

import java.util.List;
import java.util.Map;

public record SessionUser(String userId, String username, List<String> roles, List<Map<String, Object>> menus) {
    public boolean isAdmin() { return roles != null && roles.contains("R09"); }
}
