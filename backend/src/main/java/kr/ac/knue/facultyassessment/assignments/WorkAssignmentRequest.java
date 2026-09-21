package kr.ac.knue.facultyassessment.assignments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record WorkAssignmentRequest(
    String workAssignmentId,
    @NotBlank(message = "업무조직은 필수입니다.") String organizationId,
    @NotBlank(message = "담당자는 필수입니다.") String userId,
    @NotBlank(message = "담당 업무영역은 필수입니다.") String workArea,
    @NotNull(message = "지정 시작일은 필수입니다.") LocalDate effectiveStartDate,
    LocalDate effectiveEndDate,
    @NotBlank(message = "데이터 범위는 필수입니다.") String dataScopeType,
    @NotBlank(message = "처리 권한은 필수입니다.") String processPermission
) {
}
