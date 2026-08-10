package kr.ac.knue.facultyassessment.detailcodes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import kr.ac.knue.facultyassessment.common.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class DetailCodeManagementService {

    private final DetailCodeManagementMapper detailCodeManagementMapper;
    private final ObjectMapper objectMapper;

    public DetailCodeManagementService(DetailCodeManagementMapper detailCodeManagementMapper, ObjectMapper objectMapper) {
        this.detailCodeManagementMapper = detailCodeManagementMapper;
        this.objectMapper = objectMapper;
    }

    public List<DetailCodeSummary> findDetailCodes(DetailCodeSearchCriteria criteria) {
        return detailCodeManagementMapper.findDetailCodes(criteria).stream().map(this::toSummary).toList();
    }

    @Transactional
    public void saveDetailCode(String groupId, DetailCodeRequest request, String actorUserId) {
        validateUseYn(request.useYn());
        validateAdditionalAttributes(request.additionalAttributes());
        DetailCodeRow before = detailCodeManagementMapper.findDetailCodeByGroupAndCode(groupId, request.codeValue());
        String useYn = normalizedUseYn(request.useYn(), before == null ? "Y" : before.useYn());
        String additionalAttributes = serializeAttributes(request.additionalAttributes());
        if (before == null) {
            detailCodeManagementMapper.insertDetailCode(
                "DETAIL-CODE-" + UUID.randomUUID(), groupId, request, additionalAttributes, useYn
            );
        } else {
            detailCodeManagementMapper.updateDetailCode(before.detailCodeId(), request, additionalAttributes, useYn);
        }
        DetailCodeSummary after = toSummary(detailCodeManagementMapper.findDetailCodeByGroupAndCode(groupId, request.codeValue()));
        detailCodeManagementMapper.insertChangeHistory(
            "CHANGE-" + UUID.randomUUID(),
            "detail_code",
            after.detailCodeId(),
            serialize(before == null ? null : toSummary(before)),
            serialize(after),
            actorUserId,
            request.reason()
        );
    }

    private void validateUseYn(String useYn) {
        if (useYn != null && !useYn.isBlank() && !"Y".equals(useYn) && !"N".equals(useYn)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_USE_YN", "사용여부는 Y 또는 N이어야 합니다.", "useYn");
        }
    }

    private String normalizedUseYn(String useYn, String fallback) {
        return useYn == null || useYn.isBlank() ? fallback : useYn;
    }

    private void validateAdditionalAttributes(JsonNode additionalAttributes) {
        if (additionalAttributes != null && !additionalAttributes.isNull() && !additionalAttributes.isObject()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_ADDITIONAL_ATTRIBUTES", "추가속성은 JSON 객체여야 합니다.", "additionalAttributes");
        }
    }

    private String serializeAttributes(JsonNode additionalAttributes) {
        if (additionalAttributes == null || additionalAttributes.isNull()) {
            return null;
        }
        return additionalAttributes.toString();
    }

    private DetailCodeSummary toSummary(DetailCodeRow row) {
        return new DetailCodeSummary(
            row.detailCodeId(),
            row.codeValue(),
            row.codeName(),
            row.parentDetailCodeId(),
            row.displayOrder(),
            readAttributes(row.additionalAttributes()),
            row.useYn()
        );
    }

    private JsonNode readAttributes(String attributes) {
        if (attributes == null) {
            return null;
        }
        try {
            return objectMapper.readTree(attributes);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("상세코드 추가속성을 읽을 수 없습니다.", exception);
        }
    }

    private String serialize(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("상세코드 변경 추적 값을 직렬화할 수 없습니다.", exception);
        }
    }

    public record DetailCodeRow(
        String detailCodeId,
        String codeValue,
        String codeName,
        String parentDetailCodeId,
        Integer displayOrder,
        String additionalAttributes,
        String useYn
    ) {
    }

    public record DetailCodeSummary(
        String detailCodeId,
        String codeValue,
        String codeName,
        String parentDetailCodeId,
        Integer displayOrder,
        JsonNode additionalAttributes,
        String useYn
    ) {
    }
}
