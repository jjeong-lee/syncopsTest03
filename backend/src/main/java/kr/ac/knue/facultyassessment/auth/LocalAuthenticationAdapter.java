package kr.ac.knue.facultyassessment.auth;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import kr.ac.knue.facultyassessment.common.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class LocalAuthenticationAdapter implements AuthenticationPort {
    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private final AuthenticationMapper authenticationMapper;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long idleTimeoutMinutes;
    private final long absoluteTimeoutMinutes;
    public LocalAuthenticationAdapter(AuthenticationMapper authenticationMapper, @Value("${app.auth.session.idle-timeout-minutes:30}") long idleTimeoutMinutes, @Value("${app.auth.session.absolute-timeout-minutes:480}") long absoluteTimeoutMinutes) { this.authenticationMapper = authenticationMapper; this.idleTimeoutMinutes = idleTimeoutMinutes; this.absoluteTimeoutMinutes = absoluteTimeoutMinutes; }
    @Override @Transactional
    public LoginResult authenticate(String userId, String password, String ipAddress) {
        AuthenticationMapper.AccountCredentials account = authenticationMapper.findAccount(userId);
        if (account == null || !"Y".equals(account.useYn()) || !passwordMatches(password, account.passwordSalt(), account.passwordHash())) throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "사용자 ID 또는 비밀번호가 올바르지 않습니다.", null);
        AuthenticatedUser user = loadUser(account.userId()); String sessionId = createSessionId(); authenticationMapper.insertSession(sessionId, account.userId(), ipAddress); return new LoginResult(sessionId, user);
    }
    @Override @Transactional public Optional<AuthenticatedUser> findActiveSession(String sessionId) { expireSessions(); AuthenticationMapper.ActiveSession session = authenticationMapper.findActiveSession(sessionId); return session == null ? Optional.empty() : Optional.of(loadUser(session.userId())); }
    @Override public void touchSession(String sessionId) { authenticationMapper.touchSession(sessionId); }
    @Override @Transactional public void terminateSession(String sessionId, String endType, String actorUserId, String reason) { if (authenticationMapper.terminateSession(sessionId, endType) > 0) authenticationMapper.insertSessionEndHistory(sessionId, endType, actorUserId, reason); }
    @Override public List<SessionSummary> findActiveSessions() { return authenticationMapper.findActiveSessions(); }
    @Override public List<SessionEndHistory> findSessionEndHistory(String userId, OffsetDateTime startedAt, OffsetDateTime endedAt) { return authenticationMapper.findSessionEndHistory(userId, startedAt, endedAt); }
    private void expireSessions() { authenticationMapper.recordExpiredSessions(absoluteTimeoutMinutes, "ABSOLUTE_EXPIRED"); authenticationMapper.expireSessions(absoluteTimeoutMinutes, "ABSOLUTE_EXPIRED"); authenticationMapper.recordIdleExpiredSessions(idleTimeoutMinutes); authenticationMapper.expireIdleSessions(idleTimeoutMinutes); }
    private AuthenticatedUser loadUser(String userId) { List<String> roleCodes = authenticationMapper.findActiveRoleCodes(userId); List<AuthorizedMenu> menus = java.util.stream.Stream.of(roleCodes.stream().flatMap(roleCode -> authenticationMapper.findAuthorizedMenusForRole(roleCode).stream()), authenticationMapper.findAuthorizedMenusForUser(userId).stream(), authenticationMapper.findAuthorizedMenusForUserOrganizations(userId).stream()).flatMap(stream -> stream).distinct().toList(); return new AuthenticatedUser(userId, roleCodes, menus); }
    private String createSessionId() { byte[] token = new byte[32]; secureRandom.nextBytes(token); return Base64.getUrlEncoder().withoutPadding().encodeToString(token); }
    private boolean passwordMatches(String password, String salt, String expectedHash) { return MessageDigest.isEqual(Base64.getDecoder().decode(expectedHash), deriveKey(password.toCharArray(), hexToBytes(salt))); }
    private byte[] deriveKey(char[] password, byte[] salt) { try { KeySpec specification = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH); return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(specification).getEncoded(); } catch (GeneralSecurityException exception) { throw new IllegalStateException("비밀번호 검증기를 초기화할 수 없습니다.", exception); } }
    private byte[] hexToBytes(String value) { byte[] bytes = new byte[value.length() / 2]; for (int index = 0; index < value.length(); index += 2) bytes[index / 2] = (byte) Integer.parseInt(value.substring(index, index + 2), 16); return bytes; }
}
