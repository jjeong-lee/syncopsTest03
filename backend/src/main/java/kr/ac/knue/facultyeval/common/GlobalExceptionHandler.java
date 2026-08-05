package kr.ac.knue.facultyeval.common;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BusinessException.class)
  ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
    return ResponseEntity.status(ex.status()).body(ApiResponse.fail(new ApiError(ex.code(), ex.getMessage(), ex.status(), ex.fields())));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fields = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
    return ResponseEntity.badRequest().body(ApiResponse.fail(ApiError.field("입력값을 확인해 주세요.", fields)));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail(ApiError.of("INTERNAL_ERROR", "처리 중 오류가 발생했습니다.", 500)));
  }
}
