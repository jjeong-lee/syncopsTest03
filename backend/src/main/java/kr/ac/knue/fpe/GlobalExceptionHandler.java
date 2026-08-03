package kr.ac.knue.fpe;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    ResponseEntity<ApiEnvelope<Void>> badRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(ApiEnvelope.fail("VALIDATION_ERROR", ex.getMessage(), ex.fieldErrors()));
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiEnvelope<Void>> notFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiEnvelope.fail("NOT_FOUND", ex.getMessage(), List.of()));
    }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<ApiEnvelope<Void>> forbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiEnvelope.fail("FORBIDDEN", ex.getMessage(), List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiEnvelope<Void>> invalid(MethodArgumentNotValidException ex) {
        var fields = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldErrorItem(e.getField(), e.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(ApiEnvelope.fail("VALIDATION_ERROR", "입력값을 확인해주세요.", fields));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiEnvelope<Void>> typeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(ApiEnvelope.fail(
                "VALIDATION_ERROR",
                "path 또는 query parameter 형식을 확인해주세요.",
                List.of(new FieldErrorItem(ex.getName(), "요청 parameter 형식이 올바르지 않습니다."))));
    }
}
