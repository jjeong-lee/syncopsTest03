package kr.ac.knue.facultyeval.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kr.ac.knue.facultyeval.common.BusinessException;
import kr.ac.knue.facultyeval.security.CurrentUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  public static final String SESSION_COOKIE = "session_id";
  private final AuthenticationPort authPort;
  private final boolean cookieSecure;
  private final int ttlHours;

  public AuthService(AuthenticationPort authPort, @Value("${app.session.cookie-secure:false}") boolean cookieSecure, @Value("${app.session.ttl-hours:8}") int ttlHours) {
    this.authPort = authPort;
    this.cookieSecure = cookieSecure;
    this.ttlHours = ttlHours;
  }

  @Transactional
  public CurrentUser login(String loginId, String password, HttpServletResponse response) {
    if (isBlank(loginId) || isBlank(password)) {
      throw new BusinessException(400, "VALIDATION_ERROR", "로그인 ID와 비밀번호를 입력해 주세요.", Map.of("loginId", "필수값입니다.", "password", "필수값입니다."));
    }
    Map<String, Object> user = authPort.findByLoginId(loginId);
    if (user == null || !"Y".equals(String.valueOf(user.get("systemEnabled"))) || !sha256(password).equals(user.get("passwordHash"))) {
      throw new BusinessException(401, "INVALID_CREDENTIALS", "로그인 정보가 올바르지 않습니다.");
    }
    String sessionId = UUID.randomUUID().toString();
    authPort.createSession(sessionId, String.valueOf(user.get("userId")), OffsetDateTime.now().plusHours(ttlHours));
    addCookie(response, sessionId, Duration.ofHours(ttlHours));
    return currentUser(sessionId, user);
  }

  @Transactional
  public Map<String, Object> logout(String sessionId, HttpServletResponse response) {
    if (!isBlank(sessionId)) {
      authPort.revokeSession(sessionId);
    }
    addCookie(response, "", Duration.ZERO);
    return Map.of("loggedOut", true);
  }

  public CurrentUser currentUserFromRequest(HttpServletRequest request) {
    String sessionId = sessionId(request);
    if (sessionId == null) {
      return null;
    }
    Map<String, Object> user = authPort.findUserByActiveSession(sessionId);
    if (user == null) {
      return null;
    }
    return currentUser(sessionId, user);
  }

  public String sessionId(HttpServletRequest request) {
    String headerSessionId = sessionIdFromHeader(request.getHeader(HttpHeaders.COOKIE));
    if (headerSessionId != null) {
      return headerSessionId;
    }
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (SESSION_COOKIE.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }

  private String sessionIdFromHeader(String cookieHeader) {
    if (isBlank(cookieHeader)) {
      return null;
    }
    for (String part : cookieHeader.split(";")) {
      String[] nameValue = part.trim().split("=", 2);
      if (nameValue.length == 2 && SESSION_COOKIE.equals(nameValue[0]) && !isBlank(nameValue[1])) {
        return nameValue[1];
      }
    }
    return null;
  }

  private CurrentUser currentUser(String sessionId, Map<String, Object> user) {
    String userId = String.valueOf(user.get("userId"));
    List<String> roles = authPort.findRoleCodes(userId);
    return new CurrentUser(sessionId, userId, String.valueOf(user.get("loginId")), String.valueOf(user.get("displayName")), roles, authPort.findGrantedMenus(userId));
  }

  private void addCookie(HttpServletResponse response, String value, Duration maxAge) {
    String header = SESSION_COOKIE + "=" + value + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=" + maxAge.toSeconds() + (cookieSecure ? "; Secure" : "");
    response.addHeader(HttpHeaders.SET_COOKIE, header);
  }

  public static String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception ex) {
      throw new IllegalStateException("SHA-256 unavailable", ex);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
