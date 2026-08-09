package kr.ac.knue.facultyassessment.organizations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record OrganizationRelationshipRequest(
    @NotBlank(message = "상위조직은 필수입니다.") String parentOrganizationId,
    @NotNull(message = "적용 시작일은 필수입니다.") LocalDate effectiveStartDate,
    LocalDate effectiveEndDate,
    String reason
) {
}
