package kr.ac.knue.facultyassessment.menus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MenuOrderRequest(
    @NotBlank(message = "메뉴ID는 필수입니다.") String menuId,
    @NotNull(message = "표시순서는 필수입니다.") Integer displayOrder,
    String reason
) {
}
