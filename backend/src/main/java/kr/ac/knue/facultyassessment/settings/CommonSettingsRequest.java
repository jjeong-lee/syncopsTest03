package kr.ac.knue.facultyassessment.settings;

import jakarta.validation.constraints.NotNull;

public record CommonSettingsRequest(
    @NotNull(message = "세션 유휴시간은 필수입니다.") Integer sessionIdleMinutes,
    @NotNull(message = "페이지당 조회건수는 필수입니다.") Integer pageSize,
    @NotNull(message = "기본 검색기간은 필수입니다.") Integer defaultSearchPeriodDays,
    @NotNull(message = "대량조회 기준건수는 필수입니다.") Integer bulkQueryThreshold,
    @NotNull(message = "장시간작업 안내 기준은 필수입니다.") Integer longRunningWorkNoticeSeconds
) {
}
