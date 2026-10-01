package kr.ac.knue.facultyassessment.settings;

public record CommonSettings(
    int sessionIdleMinutes,
    int pageSize,
    int defaultSearchPeriodDays,
    int bulkQueryThreshold,
    int longRunningWorkNoticeSeconds
) {
}
