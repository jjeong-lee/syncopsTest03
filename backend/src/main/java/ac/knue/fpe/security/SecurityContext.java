package ac.knue.fpe.security;

import java.util.Set;

public record SecurityContext(String sessionId, String userId, Set<String> roles) {
  public boolean isSystemAdmin() { return roles.contains("R09"); }
}
