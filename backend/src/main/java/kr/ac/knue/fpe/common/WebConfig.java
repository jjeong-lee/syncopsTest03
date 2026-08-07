package kr.ac.knue.fpe.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final SessionService sessionService;
    public WebConfig(SessionService sessionService) { this.sessionService = sessionService; }
    @Override public void addInterceptors(InterceptorRegistry registry) { registry.addInterceptor(new AuthInterceptor()).addPathPatterns("/api/**"); }
    class AuthInterceptor implements HandlerInterceptor {
        @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String path = request.getRequestURI();
            if (path.equals("/api/health") || path.equals("/api/auth/login") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) return true;
            var user = sessionService.fromRequest(request).orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증 세션이 필요합니다.", Map.of()));
            if (!user.isAdmin()) throw new AppException(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다.", Map.of("role", "R09 required"));
            request.setAttribute(SessionService.REQUEST_ATTR, user);
            return true;
        }
    }
}
