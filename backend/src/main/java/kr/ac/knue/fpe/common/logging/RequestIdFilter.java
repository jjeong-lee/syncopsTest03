package kr.ac.knue.fpe.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestIdFilter.class);
    private static final Pattern REQUEST_ID_ALLOWLIST = Pattern.compile("^[A-Za-z0-9._-]{1,64}$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        String clientRequestId = safeClientRequestId(request.getHeader("X-Request-Id"));
        request.setAttribute("request_id", requestId);
        request.setAttribute("client_request_id", clientRequestId);
        response.setHeader("X-Request-Id", requestId);
        MDC.put("request_id", requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            log.info("event=api_request method={} path={} status={} request_id={}", request.getMethod(), request.getRequestURI(), response.getStatus(), requestId);
            MDC.remove("request_id");
        }
    }

    private String safeClientRequestId(String candidate) {
        if (candidate != null && REQUEST_ID_ALLOWLIST.matcher(candidate).matches()) {
            return candidate;
        }
        return "untrusted";
    }
}
