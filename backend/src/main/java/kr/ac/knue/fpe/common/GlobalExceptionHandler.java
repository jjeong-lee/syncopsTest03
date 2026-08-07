package kr.ac.knue.fpe.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse> app(AppException ex) {
        return ResponseEntity.status(ex.status()).body(ApiResponse.fail(ex.code(), ex.getMessage(), ex.details()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> validation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> details.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ApiResponse.fail("VALIDATION_ERROR", "입력값을 확인해 주세요.", details));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse> unknown(Exception ex) {
        return ResponseEntity.internalServerError().body(ApiResponse.fail("INTERNAL_ERROR", "처리 중 오류가 발생했습니다.", Map.of()));
    }
}
