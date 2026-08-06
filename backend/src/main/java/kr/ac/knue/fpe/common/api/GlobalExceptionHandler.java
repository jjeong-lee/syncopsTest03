package kr.ac.knue.fpe.common.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiResponse<Void>> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(ApiError.of("BAD_REQUEST", exception.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> safeError(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail(ApiError.of("INTERNAL_ERROR", "요청 처리 중 오류가 발생했습니다.")));
    }
}
