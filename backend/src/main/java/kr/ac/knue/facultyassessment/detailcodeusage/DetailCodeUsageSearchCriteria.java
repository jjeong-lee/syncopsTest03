package kr.ac.knue.facultyassessment.detailcodeusage;

public record DetailCodeUsageSearchCriteria(
    String groupId,
    String codeValue,
    String useYn,
    int page,
    int size
) {
}
