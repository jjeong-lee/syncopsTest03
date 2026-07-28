package kr.ac.knue.fpe.common.api;

import java.util.List;
import java.util.Map;

public record ApiError(String code, String message, List<FieldErrorItem> fieldErrors, Map<String, Object> details) {
    public record FieldErrorItem(String field, String message) {}
    public static ApiError of(String code, String message) {
        return new ApiError(code, message, List.of(), Map.of());
    }
}
