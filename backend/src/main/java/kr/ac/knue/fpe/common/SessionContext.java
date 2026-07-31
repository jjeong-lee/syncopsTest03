package kr.ac.knue.fpe.common;

import java.util.List;
import java.util.Map;

public final class SessionContext {
  private static final ThreadLocal<Map<String, Object>> USER = new ThreadLocal<>();
  private static final ThreadLocal<List<String>> ROLES = new ThreadLocal<>();
  private SessionContext() {}
  public static void set(Map<String, Object> user, List<String> roles) { USER.set(user); ROLES.set(roles); }
  public static void clear() { USER.remove(); ROLES.remove(); }
  public static Map<String, Object> user() { return USER.get(); }
  public static String userId() { return String.valueOf(USER.get().get("userId")); }
  public static List<String> roles() { return ROLES.get(); }
}
