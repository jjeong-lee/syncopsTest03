package kr.ac.knue.facultyassessment.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import kr.ac.knue.facultyassessment.externalintegrations.SchoolInformationPort;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> handleApiException(ApiException exception) {
        return ResponseEntity.status(exception.status())
            .body(ApiError.of(exception.code(), exception.getMessage(), exception.field()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String field = fieldError == null ? null : fieldError.getField();
        String message = fieldError == null ? "입력값이 올바르지 않습니다." : fieldError.getDefaultMessage();
        return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_ERROR", message, field));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_ERROR", "요청 본문이 올바르지 않습니다.", null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception) {
        return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_ERROR", "요청값이 올바르지 않습니다.", null));
    }

    @ExceptionHandler(SchoolInformationPort.ExternalLookupException.class)
    ResponseEntity<ApiError> handleExternalLookup(SchoolInformationPort.ExternalLookupException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError.of("EXTERNAL_INTEGRATION_ERROR", exception.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpectedException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError.of("INTERNAL_ERROR", "요청을 처리할 수 없습니다.", null));
    }
}
