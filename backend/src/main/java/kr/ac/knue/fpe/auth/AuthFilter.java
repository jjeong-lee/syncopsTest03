package kr.ac.knue.fpe.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.knue.fpe.common.api.ApiError;
import kr.ac.knue.fpe.common.api.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class AuthFilter extends OncePerRequestFilter {
    public static final String USER_ATTRIBUTE = "sessionUser";
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    public AuthFilter(AuthService authService, ObjectMapper objectMapper) { this.authService = authService; this.objectMapper = objectMapper; }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean publicApi = path.equals("/api/health") || path.equals("/api/auth/login");
        if (!path.startsWith("/api/") || publicApi) { chain.doFilter(request, response); return; }
        authService.current(cookie(request)).ifPresent(user -> request.setAttribute(USER_ATTRIBUTE, user));
        if (request.getAttribute(USER_ATTRIBUTE) == null) { write(response, request, 401, "UNAUTHORIZED", "인증이 필요합니다."); return; }
        SessionUser user = (SessionUser) request.getAttribute(USER_ATTRIBUTE);
        if (!user.isAdmin()) { write(response, request, 403, "FORBIDDEN", "접근 권한이 없습니다."); return; }
        chain.doFilter(request, response);
    }
    private String cookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies()).filter(c -> "JSESSIONID".equals(c.getName())).map(Cookie::getValue).findFirst().orElse(null);
    }
    private void write(HttpServletResponse response, HttpServletRequest request, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String requestId = String.valueOf(request.getAttribute("request_id"));
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message, ApiError.of(code, message), requestId));
    }
}
