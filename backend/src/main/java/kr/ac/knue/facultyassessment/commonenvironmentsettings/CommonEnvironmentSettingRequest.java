package kr.ac.knue.facultyassessment.commonenvironmentsettings;

import jakarta.validation.constraints.NotBlank;

public record CommonEnvironmentSettingRequest(
    @NotBlank(message = "설정 항목은 필수입니다.") String settingKey,
    @NotBlank(message = "설정값은 필수입니다.") String settingValue
) {
}
