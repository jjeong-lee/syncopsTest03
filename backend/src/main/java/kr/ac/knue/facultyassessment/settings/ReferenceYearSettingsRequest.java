package kr.ac.knue.facultyassessment.settings;

import jakarta.validation.constraints.NotNull;

public record ReferenceYearSettingsRequest(
    @NotNull(message = "현재 평가연도는 필수입니다.") Integer currentEvaluationYear,
    @NotNull(message = "기본 조회연도는 필수입니다.") Integer defaultSearchYear,
    @NotNull(message = "대상 연도는 필수입니다.") Integer targetYear,
    @NotNull(message = "기준정보 복사 여부는 필수입니다.") String referenceDataCopyYn,
    @NotNull(message = "초기화 여부는 필수입니다.") String initializationYn
) {
}
