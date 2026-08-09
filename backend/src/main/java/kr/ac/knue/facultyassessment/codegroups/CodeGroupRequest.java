package kr.ac.knue.facultyassessment.codegroups;

import jakarta.validation.constraints.NotBlank;

public record CodeGroupRequest(
    @NotBlank(message = "그룹ID는 필수입니다.") String groupId,
    @NotBlank(message = "명칭은 필수입니다.") String groupName,
    String description,
    String managementDepartment,
    String useYn,
    String reason
) {
}
