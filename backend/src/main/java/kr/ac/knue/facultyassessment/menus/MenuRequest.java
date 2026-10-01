package kr.ac.knue.facultyassessment.menus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.OffsetDateTime;

public record MenuRequest(
    @NotBlank(message = "메뉴명은 필수입니다.") String menuName,
    String parentMenuId,
    @NotNull(message = "표시순서는 필수입니다.") Integer displayOrder,
    @NotBlank(message = "화면ID는 필수입니다.") String screenId,
    @NotBlank(message = "URL은 필수입니다.") String url,
    String icon,
    String businessCategory,
    String description,
    @Pattern(regexp = "Y|N", message = "사용여부는 Y 또는 N이어야 합니다.") String useYn,
    OffsetDateTime exposureStartAt,
    OffsetDateTime exposureEndAt,
    String reason
) {
}
