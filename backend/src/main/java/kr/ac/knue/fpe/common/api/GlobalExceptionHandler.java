package kr.ac.knue.fpe.common.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiResponse<Object>> api(ApiException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.status()).body(ApiResponse.error(ex.getMessage(), ApiError.of(ex.code(), ex.getMessage()), requestId(request)));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Object>> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldErrorItem> fields = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new ApiError.FieldErrorItem(e.getField(), e.getDefaultMessage())).toList();
        return ResponseEntity.badRequest().body(ApiResponse.error("입력값을 확인하세요.", new ApiError("VALIDATION_ERROR", "입력값을 확인하세요.", fields, java.util.Map.of()), requestId(request)));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Object>> unknown(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("서버 오류가 발생했습니다.", ApiError.of("INTERNAL_ERROR", "서버 오류가 발생했습니다."), requestId(request)));
    }
    private String requestId(HttpServletRequest request) {
        Object id = request.getAttribute("request_id");
        return id == null ? "unknown" : id.toString();
    }
}
