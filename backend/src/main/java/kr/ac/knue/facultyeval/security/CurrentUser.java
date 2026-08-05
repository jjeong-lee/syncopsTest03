package kr.ac.knue.facultyeval.security;

import java.util.List;
import java.util.Map;

public record CurrentUser(String sessionId, String userId, String loginId, String displayName, List<String> roleCodes, List<Map<String, Object>> menus) {}
