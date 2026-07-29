package ac.knue.fpe.api;

public record ApiResponse<T>(boolean success, T data) {
  public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, data); }
  public static ApiResponse<ApiError> fail(String code, String message) { return new ApiResponse<>(false, new ApiError(code, message, java.util.Map.of())); }
  public static ApiResponse<ApiError> fail(String code, String message, java.util.Map<String, String> fieldErrors) { return new ApiResponse<>(false, new ApiError(code, message, fieldErrors)); }
}
