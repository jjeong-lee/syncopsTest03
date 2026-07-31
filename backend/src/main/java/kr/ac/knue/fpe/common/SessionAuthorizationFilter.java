package kr.ac.knue.fpe.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SessionAuthorizationFilter extends OncePerRequestFilter {
  private final CommonMapper mapper;
  private final ObjectMapper objectMapper;
  public SessionAuthorizationFilter(CommonMapper mapper, ObjectMapper objectMapper) { this.mapper = mapper; this.objectMapper = objectMapper; }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String path = request.getRequestURI();
    if (path.equals("/api/health") || path.equals("/api/auth/login") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
      chain.doFilter(request, response);
      return;
    }
    if (!path.startsWith("/api/")) { chain.doFilter(request, response); return; }
    String sessionId = sessionCookie(request);
    if (sessionId == null) { writeError(response, 401, "UNAUTHORIZED", "로그인이 필요합니다."); return; }
    Map<String, Object> user = mapper.findUserBySession(sessionId);
    if (user == null) { writeError(response, 401, "UNAUTHORIZED", "유효하지 않은 세션입니다."); return; }
    List<String> roles = mapper.findActiveRoleCodes(String.valueOf(user.get("userId")));
    if (path.startsWith("/api/") && !path.startsWith("/api/auth/") && !roles.contains("R09")) {
      writeError(response, 403, "FORBIDDEN", "권한이 없습니다."); return;
    }
    try { SessionContext.set(user, roles); chain.doFilter(request, response); }
    finally { SessionContext.clear(); }
  }

  private String sessionCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) return null;
    return Arrays.stream(cookies).filter(c -> c.getName().equals("FPE_SESSION")).map(Cookie::getValue).findFirst().orElse(null);
  }

  private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), ApiResponse.fail(ApiError.of(code, message)));
  }
}
