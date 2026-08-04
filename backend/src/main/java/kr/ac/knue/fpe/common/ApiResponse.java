package kr.ac.knue.fpe.common;

import java.time.OffsetDateTime;

public record ApiResponse<T>(boolean success, T data, ApiError error, OffsetDateTime timestamp) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, OffsetDateTime.now());
    }

    public static ApiResponse<Void> empty() {
        return new ApiResponse<>(true, null, null, OffsetDateTime.now());
    }

    public static ApiResponse<Void> fail(ApiError error) {
        return new ApiResponse<>(false, null, error, OffsetDateTime.now());
    }
}
