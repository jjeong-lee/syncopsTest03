package kr.ac.knue.fpe.common;

import java.util.Map;

public record ApiError(String code, String message, Map<String, String> fieldErrors) {
  public static ApiError of(String code, String message) { return new ApiError(code, message, Map.of()); }
  public static ApiError fields(String message, Map<String, String> fieldErrors) { return new ApiError("VALIDATION_ERROR", message, fieldErrors); }
}
