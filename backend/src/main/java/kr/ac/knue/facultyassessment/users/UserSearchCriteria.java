package kr.ac.knue.facultyassessment.users;

public record UserSearchCriteria(
    String personnelNo,
    String name,
    String organization,
    String position,
    String employmentStatus,
    String roleCode,
    String useYn
) {
}
