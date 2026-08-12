package kr.ac.knue.facultyassessment.auth;

import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class AuthenticationService {

    private final AuthenticationPort authenticationPort;

    public AuthenticationService(AuthenticationPort authenticationPort) {
        this.authenticationPort = authenticationPort;
    }

    public AuthenticationPort.LoginResult authenticate(String userId, String password, String ipAddress) {
        return authenticationPort.authenticate(userId, password, ipAddress);
    }

    public Optional<AuthenticationPort.AuthenticatedUser> findActiveSession(String sessionId) {
        return authenticationPort.findActiveSession(sessionId);
    }

    public void terminateSession(String sessionId) {
        authenticationPort.terminateSession(sessionId);
    }
}
