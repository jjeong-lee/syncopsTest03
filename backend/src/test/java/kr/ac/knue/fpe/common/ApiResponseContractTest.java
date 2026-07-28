package kr.ac.knue.fpe.common;

import kr.ac.knue.fpe.common.api.ApiError;
import kr.ac.knue.fpe.common.api.ApiResponse;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseContractTest {
    @Test void success_and_error_responses_keep_stable_envelope() {
        ApiResponse<String> ok = ApiResponse.ok("UP", "req-1");
        assertThat(ok.success()).isTrue();
        assertThat(ok.data()).isEqualTo("UP");
        assertThat(ok.requestId()).isEqualTo("req-1");
        ApiResponse<Object> error = ApiResponse.error("권한 없음", ApiError.of("FORBIDDEN", "권한 없음"), "req-2");
        assertThat(error.success()).isFalse();
        assertThat(error.error().code()).isEqualTo("FORBIDDEN");
        assertThat(error.data()).isNull();
    }
}
