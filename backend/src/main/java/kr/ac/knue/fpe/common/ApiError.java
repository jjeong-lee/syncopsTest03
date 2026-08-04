package kr.ac.knue.fpe.common;

import java.util.List;

public record ApiError(String code, String message, List<FieldViolation> fields) {
    public record FieldViolation(String field, String message) {}

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, List.of());
    }

    public static ApiError field(String code, String message, String field, String fieldMessage) {
        return new ApiError(code, message, List.of(new FieldViolation(field, fieldMessage)));
    }
}
