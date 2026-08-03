package kr.ac.knue.fpe;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Component
class PasswordHasher {
    String sha256(String raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

@Component
class SessionAuthInterceptor implements HandlerInterceptor {
    private final CommonMapper mapper;
    SessionAuthInterceptor(CommonMapper mapper) { this.mapper = mapper; }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (path.equals("/api/health") || path.equals("/api/auth/login") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return true;
        }
        String sessionId = readSessionCookie(request).orElse(null);
        if (sessionId == null) {
            writeAuthError(response, 401, "인증 세션이 필요합니다.");
            return false;
        }
        var user = mapper.findCurrentUser(sessionId);
        if (user == null || Boolean.FALSE.equals(user.get("systemEnabled"))) {
            writeAuthError(response, 401, "유효하지 않은 인증 세션입니다.");
            return false;
        }
        request.setAttribute("currentUser", user);
        if (path.startsWith("/api/admin/") && !mapper.findActiveRoleCodes((String) user.get("userId")).contains("R09")) {
            writeAuthError(response, 403, "R09 시스템관리자 권한이 필요합니다.");
            return false;
        }
        return true;
    }

    Optional<String> readSessionCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        for (Cookie cookie : request.getCookies()) {
            if ("SESSION".equals(cookie.getName())) return Optional.ofNullable(cookie.getValue());
        }
        return Optional.empty();
    }

    private void writeAuthError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String code = status == 401 ? "UNAUTHORIZED" : "FORBIDDEN";
        response.getWriter().write("{\"success\":false,\"error\":{\"code\":\"" + code + "\",\"message\":\"" + message + "\",\"fieldErrors\":[]},\"timestamp\":\"" + java.time.OffsetDateTime.now() + "\"}");
    }
}

@Component
class WebConfig implements WebMvcConfigurer {
    private final SessionAuthInterceptor interceptor;
    WebConfig(SessionAuthInterceptor interceptor) { this.interceptor = interceptor; }
    @Override public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(interceptor).addPathPatterns("/api/**"); }
}
