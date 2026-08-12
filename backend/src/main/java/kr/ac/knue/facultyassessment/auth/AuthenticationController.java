package kr.ac.knue.facultyassessment.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import kr.ac.knue.facultyassessment.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ApiResponse<CurrentUserResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        AuthenticationPort.LoginResult result = authenticationService.authenticate(request.userId(), request.password(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, sessionCookie(result.sessionId(), Duration.ofHours(8)).toString())
            .body(ApiResponse.success(CurrentUserResponse.from(result.user())));
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LoginRequest ignored, HttpServletRequest request) {
        String sessionId = readSessionId(request);
        if (sessionId != null) authenticationService.terminateSession(sessionId);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, sessionCookie("", Duration.ZERO).toString()).body(ApiResponse.success(null));
    }

    @GetMapping("/api/auth/me")
    public ApiResponse<CurrentUserResponse> currentUser(HttpServletRequest request) {
        return ApiResponse.success(CurrentUserResponse.from(actor(request)));
    }

    @GetMapping("/api/session-status/active")
    public ApiResponse<List<AuthenticationPort.SessionSummary>> activeSessions(HttpServletRequest request) {
        return ApiResponse.success(authenticationService.findActiveSessions(actor(request)));
    }

    @GetMapping("/api/session-status/history")
    public ApiResponse<List<AuthenticationPort.SessionEndHistory>> sessionEndHistory(@RequestParam(required = false) String userId,
        @RequestParam(required = false) OffsetDateTime startedAt, @RequestParam(required = false) OffsetDateTime endedAt,
        HttpServletRequest request) {
        return ApiResponse.success(authenticationService.findSessionEndHistory(actor(request), userId, startedAt, endedAt));
    }

    @PostMapping("/api/session-status/{sessionId}/force-terminate")
    public ApiResponse<Void> forceTerminateSession(@PathVariable String sessionId, @Valid @RequestBody ForceTerminateSessionRequest request,
        HttpServletRequest httpRequest) {
        authenticationService.forceTerminateSession(actor(httpRequest), sessionId, request.reason());
        return ApiResponse.success(null);
    }

    private AuthenticationPort.AuthenticatedUser actor(HttpServletRequest request) {
        return (AuthenticationPort.AuthenticatedUser) request.getAttribute(SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE);
    }

    private ResponseCookie sessionCookie(String value, Duration maxAge) {
        return ResponseCookie.from("SESSION", value).httpOnly(true).secure(secureCookie).sameSite("Lax").path("/").maxAge(maxAge).build();
    }

    private String readSessionId(HttpServletRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (jakarta.servlet.http.Cookie cookie : cookies) if ("SESSION".equals(cookie.getName())) return cookie.getValue();
        return null;
    }

    public record CurrentUserResponse(String userId, List<String> roleCodes, List<AuthenticationPort.AuthorizedMenu> menus) {
        static CurrentUserResponse from(AuthenticationPort.AuthenticatedUser user) {
            return new CurrentUserResponse(user.userId(), user.roleCodes(), user.menus());
        }
    }
}
