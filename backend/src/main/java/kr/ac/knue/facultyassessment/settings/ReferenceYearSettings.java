package kr.ac.knue.facultyassessment.settings;

public record ReferenceYearSettings(
    int currentEvaluationYear,
    int defaultSearchYear,
    int targetYear,
    String referenceDataCopyYn,
    String initializationYn
) {
}
