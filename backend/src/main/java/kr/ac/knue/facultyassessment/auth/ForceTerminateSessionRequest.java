package kr.ac.knue.facultyassessment.auth;

import jakarta.validation.constraints.NotBlank;

public record ForceTerminateSessionRequest(
    @NotBlank(message = "강제종료 사유를 입력하세요.") String reason
) {
}