package kr.ac.knue.fpe.common.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import kr.ac.knue.fpe.common.service.AuthService;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    public AuthInterceptor(AuthService authService, ObjectMapper objectMapper) { this.authService = authService; this.objectMapper = objectMapper; }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!request.getRequestURI().startsWith("/api/admin/")) return true;
        String sessionId = null;
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) if ("KNUE-FPE-SESSION".equals(cookie.getName())) sessionId = cookie.getValue();
        }
        if (authService.requireSession(sessionId) != null) return true;
        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(ApiError.of("UNAUTHORIZED", "인증이 필요합니다.")));
        return false;
    }
}
