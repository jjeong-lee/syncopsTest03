package kr.ac.knue.facultyassessment.common;

import java.util.Map;

public record ApiError(boolean success, ErrorDetail error, Map<String, Object> meta) {

    public static ApiError of(String code, String message, String field) {
        return new ApiError(false, new ErrorDetail(code, message, field), Map.of());
    }

    public record ErrorDetail(String code, String message, String field) {
    }
}
