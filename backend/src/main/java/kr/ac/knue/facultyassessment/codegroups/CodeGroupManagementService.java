package kr.ac.knue.facultyassessment.codegroups;

import com.fasterxml.jackson.core.JsonProcessingException;
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
public class CodeGroupManagementService {

    private final CodeGroupManagementMapper codeGroupManagementMapper;
    private final ObjectMapper objectMapper;

    public CodeGroupManagementService(CodeGroupManagementMapper codeGroupManagementMapper, ObjectMapper objectMapper) {
        this.codeGroupManagementMapper = codeGroupManagementMapper;
        this.objectMapper = objectMapper;
    }

    public List<CodeGroupSummary> findCodeGroups(CodeGroupSearchCriteria criteria) {
        return codeGroupManagementMapper.findCodeGroups(criteria);
    }

    @Transactional
    public void saveCodeGroup(CodeGroupRequest request, String actorUserId) {
        validateUseYn(request.useYn());
        CodeGroupSummary before = codeGroupManagementMapper.findCodeGroupById(request.groupId());
        String useYn = normalizedUseYn(request.useYn(), before == null ? "Y" : before.useYn());
        if (before == null) {
            codeGroupManagementMapper.insertCodeGroup(request, useYn);
        } else {
            codeGroupManagementMapper.updateCodeGroup(request, useYn);
        }
        CodeGroupSummary after = codeGroupManagementMapper.findCodeGroupById(request.groupId());
        codeGroupManagementMapper.insertChangeHistory(
            "CHANGE-" + UUID.randomUUID(),
            "code_group",
            after.groupId(),
            serialize(before),
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

    private String serialize(CodeGroupSummary value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("코드그룹 변경 추적 값을 직렬화할 수 없습니다.", exception);
        }
    }

    public record CodeGroupSummary(
        String groupId,
        String groupName,
        String description,
        String managementDepartment,
        String useYn
    ) {
    }
}
