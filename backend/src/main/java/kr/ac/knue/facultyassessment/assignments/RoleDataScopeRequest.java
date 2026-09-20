package kr.ac.knue.facultyassessment.assignments;

import jakarta.validation.constraints.NotBlank;

public record RoleDataScopeRequest(
    String roleDataScopeId,
    @NotBlank(message = "역할은 필수입니다.") String roleCode,
    @NotBlank(message = "데이터 범위 유형은 필수입니다.") String dataScopeType,
    String organizationCode,
    String workArea
) {
}
