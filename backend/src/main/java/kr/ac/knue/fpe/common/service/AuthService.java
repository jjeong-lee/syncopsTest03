package kr.ac.knue.fpe.common.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import kr.ac.knue.fpe.common.domain.SessionView;
import kr.ac.knue.fpe.common.persistence.AuthMapper;

@Service
public class AuthService {
    private final AuthMapper mapper;
    public AuthService(AuthMapper mapper) { this.mapper = mapper; }

    public SessionView login(String loginId, String password) {
        if (loginId == null || loginId.isBlank() || password == null || password.isBlank()) throw new IllegalArgumentException("loginId와 password는 필수입니다.");
        if (mapper.validLogin(loginId, sha256(password)) != 1) throw new IllegalArgumentException("로그인 정보를 확인해 주세요.");
        String sessionId = UUID.randomUUID().toString();
        mapper.createSession(sessionId, loginId);
        return new SessionView(sessionId, loginId, "시스템 관리자", List.of("R09"), "ACTIVE", OffsetDateTime.now());
    }

    public SessionView requireSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return null;
        String loginId = mapper.findLoginIdBySession(sessionId);
        if (loginId == null) return null;
        return new SessionView(sessionId, loginId, "시스템 관리자", List.of("R09"), "ACTIVE", OffsetDateTime.now());
    }

    public SessionView logout(String sessionId) {
        SessionView session = requireSession(sessionId);
        if (session == null) return null;
        mapper.logout(sessionId);
        mapper.recordLogout(sessionId, session.loginId());
        return new SessionView(sessionId, session.loginId(), session.userName(), session.roleCodes(), "LOGGED_OUT", session.issuedAt());
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException("hash failure"); }
    }
}
