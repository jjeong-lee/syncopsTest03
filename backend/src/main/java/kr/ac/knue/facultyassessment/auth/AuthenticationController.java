package kr.ac.knue.facultyassessment.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import kr.ac.knue.facultyassessment.common.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final boolean secureCookie;

    public AuthenticationController(AuthenticationService authenticationService, @Value("${app.auth.session.secure:false}") boolean secureCookie) {
        this.authenticationService = authenticationService;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationPort.LoginResult result = authenticationService.authenticate(request.userId(), request.password());
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, sessionCookie(result.sessionId(), Duration.ofHours(8)).toString())
            .body(ApiResponse.success(CurrentUserResponse.from(result.user())));
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LoginRequest ignored, HttpServletRequest request) {
        String sessionId = readSessionId(request);
        if (sessionId != null) {
            authenticationService.terminateSession(sessionId);
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, sessionCookie("", Duration.ZERO).toString())
            .body(ApiResponse.success(null));
    }

    @GetMapping("/api/auth/me")
    public ApiResponse<CurrentUserResponse> currentUser(HttpServletRequest request) {
        AuthenticationPort.AuthenticatedUser user = (AuthenticationPort.AuthenticatedUser) request.getAttribute(
            SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE
        );
        return ApiResponse.success(CurrentUserResponse.from(user));
    }

    private ResponseCookie sessionCookie(String value, Duration maxAge) {
        return ResponseCookie.from("SESSION", value)
            .httpOnly(true)
            .secure(secureCookie)
            .sameSite("Lax")
            .path("/")
            .maxAge(maxAge)
            .build();
    }

    private String readSessionId(HttpServletRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            if ("SESSION".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public record CurrentUserResponse(String userId, List<String> roleCodes, List<AuthenticationPort.AuthorizedMenu> menus) {
        static CurrentUserResponse from(AuthenticationPort.AuthenticatedUser user) {
            return new CurrentUserResponse(user.userId(), user.roleCodes(), user.menus());
        }
    }
}
