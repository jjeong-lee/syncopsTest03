package kr.ac.knue.fpe.auth;

import java.util.List;
import java.util.UUID;

public record SessionUser(UUID userId, String loginId, String displayName, List<String> roleCodes) {
    public boolean isAdmin() { return roleCodes.contains("R09"); }
}
