package kr.ac.knue.facultyassessment.referenceyearmanagement;

import jakarta.validation.constraints.NotNull;

public record ReferenceYearSettingRequest(
    @NotNull(message = "현재 평가연도는 필수입니다.") Integer currentEvaluationYear,
    @NotNull(message = "기본 조회연도는 필수입니다.") Integer defaultQueryYear,
    @NotNull(message = "대상 연도는 필수입니다.") Integer targetYear,
    @NotNull(message = "기준정보 복사 여부는 필수입니다.") String baselineCopyYn,
    @NotNull(message = "초기화 여부는 필수입니다.") String initializationYn
) {
}
