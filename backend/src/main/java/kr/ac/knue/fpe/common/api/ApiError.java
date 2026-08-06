package kr.ac.knue.fpe.common.api;

import java.util.Map;

public record ApiError(String code, String message, Map<String, String> fieldErrors) {
    public static ApiError of(String code, String message) { return new ApiError(code, message, Map.of()); }
    public static ApiError validation(Map<String, String> fieldErrors) { return new ApiError("VALIDATION_FAILED", "입력값을 확인해 주세요.", fieldErrors); }
}
