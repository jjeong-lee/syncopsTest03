package kr.ac.knue.facultyassessment.menuusage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record MenuUsageSettingRequest(
    @NotBlank(message = "메뉴 ID는 필수입니다.") String menuId,
    @NotBlank(message = "사용여부는 필수입니다.") String useYn,
    @NotNull(message = "노출 시작일시는 필수입니다.") OffsetDateTime exposureStartAt,
    OffsetDateTime exposureEndAt
) {
}
