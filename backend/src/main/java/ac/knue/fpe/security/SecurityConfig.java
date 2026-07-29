package ac.knue.fpe.security;

import ac.knue.fpe.api.ApiResponse;
import ac.knue.fpe.persistence.FoundationMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class SecurityConfig implements WebMvcConfigurer {
  private final FoundationMapper mapper;
  private final ObjectMapper objectMapper;

  public SecurityConfig(FoundationMapper mapper, ObjectMapper objectMapper) {
    this.mapper = mapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new SessionInterceptor(mapper, objectMapper)).addPathPatterns("/api/**");
  }

  static class SessionInterceptor implements HandlerInterceptor {
    private final FoundationMapper mapper;
    private final ObjectMapper objectMapper;
    SessionInterceptor(FoundationMapper mapper, ObjectMapper objectMapper) { this.mapper = mapper; this.objectMapper = objectMapper; }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
      String path = request.getRequestURI();
      if (path.equals("/api/health") || path.equals("/api/auth/login")) return true;
      String sessionId = Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
          .filter(c -> c.getName().equals("FPESESSION")).map(Cookie::getValue).findFirst().orElse(null);
      if (sessionId == null || mapper.findSessionUserId(sessionId) == null) {
        write(response, 401, "UNAUTHORIZED", "로그인이 필요합니다.");
        return false;
      }
      String userId = mapper.findSessionUserId(sessionId);
      Set<String> roles = mapper.findActiveRoleCodes(userId).stream().collect(Collectors.toSet());
      request.setAttribute("securityContext", new SecurityContext(sessionId, userId, roles));
      if (!path.equals("/api/auth/me") && !path.equals("/api/auth/logout") && !roles.contains("R09")) {
        write(response, 403, "FORBIDDEN", "접근 권한이 없습니다.");
        return false;
      }
      return true;
    }

    private void write(HttpServletResponse response, int status, String code, String message) throws Exception {
      response.setStatus(status);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      objectMapper.writeValue(response.getWriter(), ApiResponse.fail(code, message));
    }
  }
}
