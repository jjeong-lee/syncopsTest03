package kr.ac.knue.fpe.ports;

public interface AuthenticationPort {
    boolean matches(String rawPassword, String storedHash);
    String hash(String rawPassword);
}
