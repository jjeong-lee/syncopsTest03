package kr.ac.knue.facultyassessment.roles;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RoleRequest(
    @NotBlank(message = "역할코드는 필수입니다.")
    @Pattern(regexp = "R0[1-9]", message = "등록된 역할코드만 사용할 수 있습니다.") String roleCode,
    @NotBlank(message = "역할명은 필수입니다.") String roleName,
    @NotBlank(message = "역할별 목적은 필수입니다.") String purpose,
    String assignmentCriteria,
    String defaultDataScope,
    String reason
) {
}
