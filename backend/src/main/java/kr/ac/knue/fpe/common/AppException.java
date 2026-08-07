package kr.ac.knue.fpe.common;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class AppException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    private final Map<String, String> details;
    public AppException(HttpStatus status, String code, String message, Map<String, String> details) {
        super(message); this.status = status; this.code = code; this.details = details;
    }
    public HttpStatus status() { return status; }
    public String code() { return code; }
    public Map<String, String> details() { return details; }
}
