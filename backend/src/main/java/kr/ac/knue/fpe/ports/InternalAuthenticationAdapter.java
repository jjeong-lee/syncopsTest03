package kr.ac.knue.fpe.ports;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;

@Component
public class InternalAuthenticationAdapter implements AuthenticationPort {
    @Override
    public boolean matches(String rawPassword, String storedHash) {
        return hash(rawPassword).equals(storedHash);
    }

    @Override
    public String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(("knue-fpe:" + rawPassword).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : encoded) builder.append(String.format("%02x", b));
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
