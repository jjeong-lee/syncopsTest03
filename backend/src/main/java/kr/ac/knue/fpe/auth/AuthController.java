package kr.ac.knue.fpe.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import kr.ac.knue.fpe.common.api.ApiException;
import kr.ac.knue.fpe.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
    @PostMapping("/login")
    ApiResponse<SessionUser> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        AuthService.LoginResult result = authService.login(request.loginId(), request.password(), String.valueOf(httpRequest.getAttribute("request_id")));
        Cookie cookie = new Cookie("JSESSIONID", result.sessionId());
        cookie.setHttpOnly(true); cookie.setPath("/"); cookie.setMaxAge(8 * 60 * 60);
        response.addCookie(cookie);
        response.addHeader("Set-Cookie", "JSESSIONID=" + result.sessionId() + "; Path=/; Max-Age=28800; HttpOnly; SameSite=Lax");
        return ApiResponse.ok(result.user(), requestId(httpRequest));
    }
    @PostMapping("/logout")
    ApiResponse<Object> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(cookie(request));
        response.addHeader("Set-Cookie", "JSESSIONID=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        return ApiResponse.ok(null, requestId(request));
    }
    @GetMapping("/me")
    ApiResponse<SessionUser> me(HttpServletRequest request) {
        return ApiResponse.ok((SessionUser) request.getAttribute(AuthFilter.USER_ATTRIBUTE), requestId(request));
    }
    private String requestId(HttpServletRequest request) { return String.valueOf(request.getAttribute("request_id")); }
    private String cookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies()).filter(c -> "JSESSIONID".equals(c.getName())).map(Cookie::getValue).findFirst().orElse(null);
    }
    record LoginRequest(@NotBlank String loginId, @NotBlank String password) {}
}
