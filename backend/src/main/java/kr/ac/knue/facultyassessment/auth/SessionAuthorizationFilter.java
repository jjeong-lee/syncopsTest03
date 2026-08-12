package kr.ac.knue.facultyassessment.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.ac.knue.facultyassessment.common.ApiError;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class SessionAuthorizationFilter extends OncePerRequestFilter {

    public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";
    private final AuthenticationPort authenticationPort;
    private final MenuAuthorizationService menuAuthorizationService;
    private final ObjectMapper objectMapper;

    public SessionAuthorizationFilter(AuthenticationPort authenticationPort, MenuAuthorizationService menuAuthorizationService, ObjectMapper objectMapper) {
        this.authenticationPort = authenticationPort;
        this.menuAuthorizationService = menuAuthorizationService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/health") || path.equals("/api/auth/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String sessionId = readSessionId(request);
        AuthenticationPort.AuthenticatedUser user = sessionId == null ? null : authenticationPort.findActiveSession(sessionId).orElse(null);
        if (user == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHENTICATED", "인증 세션이 필요합니다.");
            return;
        }
        authenticationPort.touchSession(sessionId);
        if (!request.getRequestURI().startsWith("/api/auth/") && !menuAuthorizationService.canAccess(user, request.getRequestURI())) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다.");
            return;
        }
        request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, user);
        filterChain.doFilter(request, response);
    }

    private String readSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if ("SESSION".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiError.of(code, message, null));
    }
}
