package kr.ac.knue.facultyassessment.detailcodes;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DetailCodeRequest(
    @NotBlank(message = "코드값은 필수입니다.") String codeValue,
    @NotBlank(message = "코드명은 필수입니다.") String codeName,
    String parentDetailCodeId,
    @NotNull(message = "정렬순서는 필수입니다.") Integer displayOrder,
    JsonNode additionalAttributes,
    String useYn,
    String reason
) {
}
