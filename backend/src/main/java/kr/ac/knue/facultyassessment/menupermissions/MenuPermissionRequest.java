package kr.ac.knue.facultyassessment.menupermissions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MenuPermissionRequest(
    @NotBlank(message = "대상 구분은 필수입니다.")
    @Pattern(regexp = "ROLE|ORGANIZATION|USER", message = "대상 구분은 ROLE, ORGANIZATION, USER만 사용할 수 있습니다.") String subjectType,
    @NotBlank(message = "대상 ID는 필수입니다.") String subjectId,
    @NotBlank(message = "메뉴 ID는 필수입니다.") String menuId,
    @NotBlank(message = "접근 허용 여부는 필수입니다.")
    @Pattern(regexp = "Y|N", message = "접근 허용 여부는 Y 또는 N이어야 합니다.") String accessAllowed,
    String reason
) {
}
