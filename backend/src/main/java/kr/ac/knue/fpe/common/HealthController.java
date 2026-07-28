package kr.ac.knue.fpe.common;

import jakarta.servlet.http.HttpServletRequest;
import kr.ac.knue.fpe.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/api/health")
    ApiResponse<Map<String,String>> health(HttpServletRequest request) {
        return ApiResponse.ok(Map.of("status", "UP"), String.valueOf(request.getAttribute("request_id")));
    }
}
