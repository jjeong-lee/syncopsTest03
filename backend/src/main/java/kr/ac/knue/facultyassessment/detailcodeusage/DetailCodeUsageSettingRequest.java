package kr.ac.knue.facultyassessment.detailcodeusage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DetailCodeUsageSettingRequest(
    @NotBlank(message = "코드그룹은 필수입니다.") String groupId,
    @NotBlank(message = "코드값은 필수입니다.") String codeValue,
    @NotBlank(message = "사용여부는 필수입니다.") String useYn,
    @NotNull(message = "적용 시작일은 필수입니다.") LocalDate effectiveStartDate,
    LocalDate effectiveEndDate
) {
}
