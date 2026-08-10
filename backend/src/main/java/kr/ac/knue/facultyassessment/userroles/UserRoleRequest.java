package kr.ac.knue.facultyassessment.userroles;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UserRoleRequest(
    @NotBlank(message = "역할코드는 필수입니다.") String roleCode,
    @NotBlank(message = "승인자는 필수입니다.") String approvalUserId,
    @NotNull(message = "유효 시작일은 필수입니다.") LocalDate effectiveStartDate,
    LocalDate effectiveEndDate,
    String reason
) {
}
