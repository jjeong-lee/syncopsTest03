package kr.ac.knue.fpe.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import kr.ac.knue.fpe.persistence.ManagementMapper;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final ManagementMapper mapper;
    private final ObjectMapper objectMapper;
    public AuthInterceptor(ManagementMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/") || path.equals("/api/health") || path.equals("/api/auth/login")) {
            return true;
        }
        String sessionId = findCookie(request, "SESSION");
        if (sessionId == null || sessionId.isBlank()) {
            writeError(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다.");
            return false;
        }
        var session = mapper.findActiveSession(sessionId);
        if (session == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "세션이 만료되었거나 유효하지 않습니다.");
            return false;
        }
        String userId = String.valueOf(session.get("userId"));
        String username = String.valueOf(session.get("username"));
        var roles = mapper.selectRolesForUser(userId);
        var menus = mapper.selectMenusForUser(userId);
        mapper.touchSession(sessionId);
        SessionContext.set(new SessionUser(userId, username, roles, menus));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SessionContext.clear();
    }

    private String findCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies).filter(c -> name.equals(c.getName())).map(Cookie::getValue).findFirst().orElse(null);
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        var body = ApiResponse.fail(new ApiError(code, message, List.of()));
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
