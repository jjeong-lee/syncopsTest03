package kr.ac.knue.facultyeval.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.knue.facultyeval.common.ApiResponse;
import kr.ac.knue.facultyeval.common.BusinessException;
import kr.ac.knue.facultyeval.security.CurrentUser;
import kr.ac.knue.facultyeval.security.CurrentUserHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<CurrentUser>> login(@RequestBody LoginRequest request, HttpServletResponse response) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(authService.login(request.loginId(), request.password(), response)));
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Object>> logout(HttpServletRequest request, HttpServletResponse response) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(authService.logout(authService.sessionId(request), response)));
  }

  @GetMapping("/me")
  public ApiResponse<CurrentUser> me() {
    CurrentUser user = CurrentUserHolder.get();
    if (user == null) {
      throw new BusinessException(401, "UNAUTHENTICATED", "인증 세션이 필요합니다.");
    }
    return ApiResponse.ok(user);
  }

  public record LoginRequest(String loginId, String password) {}
}
