package kr.ac.knue.fpe.common;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import kr.ac.knue.fpe.common.logging.RequestIdFilter;

import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

class RequestLoggingTest {
    @Test void request_id_filter_and_admin_mutation_logs_include_request_id() throws Exception {
        String filter = Files.readString(Path.of("src/main/java/kr/ac/knue/fpe/common/logging/RequestIdFilter.java"));
        String service = Files.readString(Path.of("src/main/java/kr/ac/knue/fpe/admin/AdminService.java"));
        assertThat(filter).contains("X-Request-Id", "request_id", "event=api_request");
        assertThat(service).contains("event=admin_mutation", "request_id={}");
    }

    @Test void request_id_header_uses_safe_fallback_when_request_header_contains_invalid_characters() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        request.addHeader("X-Request-Id", "bad\r\nX-Evil: injected");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new RequestIdFilter().doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader("X-Request-Id"))
            .isNotEqualTo("bad\r\nX-Evil: injected")
            .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        assertThat(request.getAttribute("request_id")).isEqualTo(response.getHeader("X-Request-Id"));
        assertThat(request.getAttribute("client_request_id")).isEqualTo("untrusted");
    }

    @Test void response_request_id_is_server_generated_even_when_request_header_is_allowlisted() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        request.addHeader("X-Request-Id", "client-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new RequestIdFilter().doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader("X-Request-Id"))
            .isNotEqualTo("client-123")
            .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        assertThat(request.getAttribute("request_id")).isEqualTo(response.getHeader("X-Request-Id"));
        assertThat(request.getAttribute("client_request_id")).isEqualTo("client-123");
    }
}
