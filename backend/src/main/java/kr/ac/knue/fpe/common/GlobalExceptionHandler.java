package kr.ac.knue.fpe.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(BusinessException.class)
  ResponseEntity<ApiResponse<Void>> business(BusinessException ex) {
    ApiError error = ex.fieldErrors().isEmpty()
        ? ApiError.of(ex.status().name(), ex.getMessage())
        : ApiError.fields(ex.getMessage(), ex.fieldErrors());
    return ResponseEntity.status(ex.status()).body(ApiResponse.fail(error));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException ex) {
    Map<String, String> fields = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(e -> fields.put(e.getField(), e.getDefaultMessage()));
    return ResponseEntity.badRequest().body(ApiResponse.fail(ApiError.fields("입력값을 확인해 주세요.", fields)));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiResponse<Void>> generic(Exception ex) {
    return ResponseEntity.internalServerError().body(ApiResponse.fail(ApiError.of("INTERNAL_ERROR", "처리 중 오류가 발생했습니다.")));
  }
}
