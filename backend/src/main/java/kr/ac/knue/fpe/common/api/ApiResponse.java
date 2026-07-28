package kr.ac.knue.fpe.common.api;

public record ApiResponse<T>(boolean success, String message, T data, ApiError error, String requestId) {
    public static <T> ApiResponse<T> ok(T data, String requestId) {
        return new ApiResponse<>(true, "요청이 정상 처리되었습니다.", data, null, requestId);
    }
    public static ApiResponse<Object> error(String message, ApiError error, String requestId) {
        return new ApiResponse<>(false, message, null, error, requestId);
    }
}
