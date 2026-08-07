package kr.ac.knue.fpe.common;

import java.util.List;

public record SessionUser(String loginId, String userName, List<String> roleCodes) {
    public boolean isAdmin() { return roleCodes != null && roleCodes.contains("R09"); }
}
