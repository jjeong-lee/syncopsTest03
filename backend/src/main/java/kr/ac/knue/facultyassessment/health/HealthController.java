package kr.ac.knue.facultyassessment.health;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping(path = "/api/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse health() {
        return new ApiResponse(true, Map.of("status", "UP"), Map.of());
    }

    public record ApiResponse(boolean success, Map<String, String> data, Map<String, Object> meta) {
    }
}
