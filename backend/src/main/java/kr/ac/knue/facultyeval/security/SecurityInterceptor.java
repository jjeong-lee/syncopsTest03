package kr.ac.knue.facultyeval.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.knue.facultyeval.common.BusinessException;
import kr.ac.knue.facultyeval.auth.AuthService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SecurityInterceptor implements HandlerInterceptor {
  private final AuthService authService;

  public SecurityInterceptor(AuthService authService) {
    this.authService = authService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String path = request.getRequestURI();
    if (path.equals("/api/health") || path.equals("/api/auth/login") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
      return true;
    }
    CurrentUser user = authService.currentUserFromRequest(request);
    if (user == null) {
      throw new BusinessException(401, "UNAUTHENTICATED", "인증 세션이 필요합니다.");
    }
    CurrentUserHolder.set(user);
    if (path.startsWith("/api/") && !path.equals("/api/auth/me") && !path.equals("/api/auth/logout")) {
      authorize(path, request.getMethod(), user);
    }
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    CurrentUserHolder.clear();
  }

  private void authorize(String path, String method, CurrentUser user) {
    if (user.roleCodes().contains("R09")) {
      return;
    }
    if ((user.roleCodes().contains("R04") || user.roleCodes().contains("R08")) && method.equals("GET")) {
      return;
    }
    throw new BusinessException(403, "FORBIDDEN", "해당 메뉴 권한이 없습니다.");
  }
}
