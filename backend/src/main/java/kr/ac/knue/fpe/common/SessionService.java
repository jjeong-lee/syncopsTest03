package kr.ac.knue.fpe.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SessionService {
    public static final String REQUEST_ATTR = "sessionUser";
    private final ManagedRecordMapper mapper;
    private final String cookieName;
    private final String localAdminPassword;
    public SessionService(ManagedRecordMapper mapper, @Value("${app.session-cookie-name}") String cookieName, @Value("${app.local-admin-password}") String localAdminPassword) {
        this.mapper = mapper; this.cookieName = cookieName; this.localAdminPassword = localAdminPassword;
    }
    public SessionUser login(String loginId, String password, HttpServletRequest request, HttpServletResponse response) {
        if (!"admin".equals(loginId) || !localAdminPassword.equals(password)) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인 정보가 올바르지 않습니다.", Map.of());
        }
        String id = UUID.randomUUID().toString();
        mapper.createSession(id, loginId, "시스템관리자", "R09", request.getRemoteAddr());
        Cookie cookie = new Cookie(cookieName, id);
        cookie.setHttpOnly(true); cookie.setPath("/"); cookie.setMaxAge(60 * 60 * 8);
        response.addCookie(cookie);
        return new SessionUser(loginId, "시스템관리자", List.of("R09"));
    }
    public Optional<SessionUser> fromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                Map<String, Object> session = mapper.session(cookie.getValue());
                if (session == null) return Optional.empty();
                return Optional.of(new SessionUser(Objects.toString(session.get("loginId")), Objects.toString(session.get("userName")), List.of(Objects.toString(session.get("roleCode")))));
            }
        }
        return Optional.empty();
    }
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) if (cookieName.equals(cookie.getName())) mapper.endSession(cookie.getValue(), "LOGOUT");
        }
        Cookie cleared = new Cookie(cookieName, ""); cleared.setPath("/"); cleared.setMaxAge(0); response.addCookie(cleared);
    }
}
