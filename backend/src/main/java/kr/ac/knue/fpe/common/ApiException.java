package kr.ac.knue.fpe.common;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final ApiError error;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.error = ApiError.of(code, message);
    }

    public ApiException(HttpStatus status, ApiError error) {
        super(error.message());
        this.status = status;
        this.error = error;
    }

    public HttpStatus status() { return status; }
    public ApiError error() { return error; }
}
