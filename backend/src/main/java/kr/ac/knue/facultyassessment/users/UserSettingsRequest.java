package kr.ac.knue.facultyassessment.users;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public record UserSettingsRequest(
    @NotNull(message = "사용여부는 필수입니다.")
    @Pattern(regexp = "Y|N", message = "사용여부는 Y 또는 N이어야 합니다.")
    String useYn,
    @NotEmpty(message = "업무 역할은 하나 이상 선택해야 합니다.")
    List<@Pattern(regexp = "R0[1-9]", message = "업무 역할이 올바르지 않습니다.") String> roleCodes,
    String reason
) {
}
