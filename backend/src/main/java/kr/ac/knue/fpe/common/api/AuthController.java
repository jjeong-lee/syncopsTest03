package kr.ac.knue.fpe.common.api;

import java.time.Duration;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import kr.ac.knue.fpe.common.domain.SessionView;
import kr.ac.knue.fpe.common.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<SessionView>> login(@RequestBody Map<String, String> body) {
        SessionView session = authService.login(body.get("loginId"), body.get("password"));
        ResponseCookie cookie = ResponseCookie.from("KNUE-FPE-SESSION", session.sessionId()).path("/").httpOnly(true).sameSite("Lax").maxAge(Duration.ofHours(8)).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(ApiResponse.ok(session));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<SessionView>> logout(@CookieValue(name = "KNUE-FPE-SESSION", required = false) String sessionId) {
        SessionView session = authService.logout(sessionId);
        if (session == null) return ResponseEntity.status(401).body(ApiResponse.fail(ApiError.of("UNAUTHORIZED", "인증이 필요합니다.")));
        return ResponseEntity.ok(ApiResponse.ok(session));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SessionView>> me(@CookieValue(name = "KNUE-FPE-SESSION", required = false) String sessionId) {
        SessionView session = authService.requireSession(sessionId);
        if (session == null) return ResponseEntity.status(401).body(ApiResponse.fail(ApiError.of("UNAUTHORIZED", "인증이 필요합니다.")));
        return ResponseEntity.ok(ApiResponse.ok(session));
    }
}
