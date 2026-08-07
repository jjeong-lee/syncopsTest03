package kr.ac.knue.fpe.common;

import java.util.Map;

public record ApiResponse(boolean success, Object data, ApiError error) {
    public static ApiResponse ok(Object data) { return new ApiResponse(true, data, null); }
    public static ApiResponse fail(String code, String message, Map<String, String> details) { return new ApiResponse(false, null, new ApiError(code, message, details)); }
    public record ApiError(String code, String message, Map<String, String> details) {}
}
