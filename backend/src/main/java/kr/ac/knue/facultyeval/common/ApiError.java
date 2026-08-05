package kr.ac.knue.facultyeval.common;

import java.util.Map;

public record ApiError(String code, String message, int status, Map<String, String> fields) {
  public static ApiError of(String code, String message, int status) {
    return new ApiError(code, message, status, Map.of());
  }

  public static ApiError field(String message, Map<String, String> fields) {
    return new ApiError("VALIDATION_ERROR", message, 400, fields);
  }
}
