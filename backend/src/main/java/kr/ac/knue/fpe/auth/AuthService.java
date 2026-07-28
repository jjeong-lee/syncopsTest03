package kr.ac.knue.fpe.auth;

import kr.ac.knue.fpe.common.api.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthMapper mapper;
    public AuthService(AuthMapper mapper) { this.mapper = mapper; }
    @Transactional
    public LoginResult login(String loginId, String password, String requestId) {
        Map<String,Object> account = mapper.findAccount(loginId);
        if (account == null || !Objects.equals(account.get("accountStatus"), "ACTIVE") || !Objects.equals(account.get("systemUseYn"), "Y") || !matches(password, String.valueOf(account.get("passwordHash")))) {
            log.warn("event=auth_failure login_id={} request_id={}", loginId, requestId);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "아이디 또는 비밀번호를 확인하세요.");
        }
        UUID userId = (UUID) account.get("userId");
        String sessionId = UUID.randomUUID().toString();
        mapper.createSession(sessionId, userId, LocalDateTime.now(), LocalDateTime.now().plusHours(8));
        SessionUser user = new SessionUser(userId, loginId, String.valueOf(account.get("displayName")), mapper.roleCodes(userId));
        log.info("event=auth_success login_id={} request_id={}", loginId, requestId);
        return new LoginResult(sessionId, user);
    }
    public Optional<SessionUser> current(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return Optional.empty();
        Map<String,Object> account = mapper.findBySession(sessionId);
        if (account == null) return Optional.empty();
        UUID userId = (UUID) account.get("userId");
        return Optional.of(new SessionUser(userId, String.valueOf(account.get("loginId")), String.valueOf(account.get("displayName")), mapper.roleCodes(userId)));
    }
    @Transactional public void logout(String sessionId) { if (sessionId != null) mapper.revoke(sessionId); }
    private boolean matches(String raw, String hash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b: digest) sb.append(String.format("%02x", b));
            return sb.toString().equals(hash);
        } catch (Exception ex) { return false; }
    }
    public record LoginResult(String sessionId, SessionUser user) {}
}
