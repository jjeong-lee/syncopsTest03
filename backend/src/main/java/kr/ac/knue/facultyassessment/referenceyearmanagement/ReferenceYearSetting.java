package kr.ac.knue.facultyassessment.referenceyearmanagement;

public record ReferenceYearSetting(
    Integer currentEvaluationYear,
    Integer defaultQueryYear,
    Integer targetYear,
    String baselineCopyYn,
    String initializationYn
) {
}
