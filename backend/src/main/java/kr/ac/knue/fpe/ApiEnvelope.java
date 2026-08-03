package kr.ac.knue.fpe;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiEnvelope<T>(boolean success, T data, ApiError error, OffsetDateTime timestamp, String traceId) {
    public static <T> ApiEnvelope<T> ok(T data) {
        return new ApiEnvelope<>(true, data, null, OffsetDateTime.now(), null);
    }

    public static ApiEnvelope<Void> ok() {
        return new ApiEnvelope<>(true, null, null, OffsetDateTime.now(), null);
    }

    public static ApiEnvelope<Void> fail(String code, String message, List<FieldErrorItem> fieldErrors) {
        return new ApiEnvelope<>(false, null, new ApiError(code, message, fieldErrors == null ? List.of() : fieldErrors), OffsetDateTime.now(), null);
    }
}

record ApiError(String code, String message, List<FieldErrorItem> fieldErrors) {}
record FieldErrorItem(String field, String message) {}
class BadRequestException extends RuntimeException {
    private final List<FieldErrorItem> fieldErrors;
    BadRequestException(String field, String message) {
        super("입력값을 확인해주세요.");
        this.fieldErrors = List.of(new FieldErrorItem(field, message));
    }
    List<FieldErrorItem> fieldErrors() { return fieldErrors; }
}
class NotFoundException extends RuntimeException {
    NotFoundException(String message) { super(message); }
}
class ForbiddenException extends RuntimeException {
    ForbiddenException(String message) { super(message); }
}
