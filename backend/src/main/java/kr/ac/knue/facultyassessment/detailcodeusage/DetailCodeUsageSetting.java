package kr.ac.knue.facultyassessment.detailcodeusage;

import java.time.LocalDate;

public record DetailCodeUsageSetting(
    String groupId,
    String codeValue,
    String useYn,
    LocalDate effectiveStartDate,
    LocalDate effectiveEndDate
) {
}
