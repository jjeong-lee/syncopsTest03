package kr.ac.knue.fpe.common;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
  private final HttpStatus status;
  private final Map<String, String> fieldErrors;
  public BusinessException(HttpStatus status, String message) { this(status, message, Map.of()); }
  public BusinessException(HttpStatus status, String message, Map<String, String> fieldErrors) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
  public HttpStatus status() { return status; }
  public Map<String, String> fieldErrors() { return fieldErrors; }
}
