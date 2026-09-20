package kr.ac.knue.facultyassessment.assignments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PositionAssignmentRequest(
    String positionAssignmentId,
    @NotBlank(message = "보직코드는 필수입니다.") String positionCode,
    @NotBlank(message = "대상 사용자는 필수입니다.") String userId,
    @NotBlank(message = "소속조직은 필수입니다.") String organizationId,
    @NotNull(message = "유효 시작일은 필수입니다.") LocalDate effectiveStartDate,
    LocalDate effectiveEndDate
) {
}
