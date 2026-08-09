package kr.ac.knue.facultyassessment.menus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MenuRequest(
    @NotBlank(message = "메뉴명은 필수입니다.") String menuName,
    String parentMenuId,
    @NotNull(message = "표시순서는 필수입니다.") Integer displayOrder,
    @NotBlank(message = "화면ID는 필수입니다.") String screenId,
    @NotBlank(message = "URL은 필수입니다.") String url,
    String icon,
    String businessCategory,
    String description,
    String useYn,
    String reason
) {
}
