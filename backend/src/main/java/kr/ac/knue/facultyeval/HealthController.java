package kr.ac.knue.facultyeval;

import java.time.OffsetDateTime;
import java.util.Map;
import kr.ac.knue.facultyeval.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  @GetMapping("/api/health")
  public ApiResponse<Map<String, Object>> health() {
    return ApiResponse.ok(Map.of("status", "UP", "service", "faculty-evaluation-common", "checkedAt", OffsetDateTime.now().toString()));
  }
}
