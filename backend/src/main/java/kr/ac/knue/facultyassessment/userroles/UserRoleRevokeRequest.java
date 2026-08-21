package kr.ac.knue.facultyassessment.userroles;

import jakarta.validation.constraints.NotBlank;

public record UserRoleRevokeRequest(
    @NotBlank(message = "승인자는 필수입니다.") String approvalUserId,
    String reason
) {
}
