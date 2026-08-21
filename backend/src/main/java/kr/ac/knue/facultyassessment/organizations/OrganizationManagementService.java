package kr.ac.knue.facultyassessment.organizations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import kr.ac.knue.facultyassessment.common.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class OrganizationManagementService {

    private final OrganizationManagementMapper organizationManagementMapper;
    private final ObjectMapper objectMapper;

    public OrganizationManagementService(OrganizationManagementMapper organizationManagementMapper, ObjectMapper objectMapper) {
        this.organizationManagementMapper = organizationManagementMapper;
        this.objectMapper = objectMapper;
    }

    public List<OrganizationSummary> findOrganizations(OrganizationSearchCriteria criteria) {
        List<OrganizationRow> rows = organizationManagementMapper.findOrganizations(criteria);
        return rows.stream().map(row -> new OrganizationSummary(
            row.organizationId(),
            row.organizationCode(),
            row.organizationName(),
            row.organizationType(),
            row.parentOrganizationId(),
            row.parentOrganizationCode(),
            row.parentOrganizationName(),
            row.effectiveStartDate(),
            row.effectiveEndDate(),
            rows.stream()
                .filter(child -> row.organizationId().equals(child.parentOrganizationId()))
                .map(child -> new OrganizationNode(child.organizationId(), child.organizationCode(), child.organizationName()))
                .toList()
        )).toList();
    }

    /**
     * 현재 관계와 분리해 이전 관계도 조회하여, 조직 구조 변경이 과거 적용기간을 덮어쓰지 않게 한다.
     */
    public List<OrganizationRelationship> findRelationshipHistory(String organizationId) {
        if (!organizationManagementMapper.organizationExists(organizationId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", "조직을 찾을 수 없습니다.", "organizationId");
        }
        return organizationManagementMapper.findRelationshipHistory(organizationId);
    }

    @Transactional
    public void saveRelationship(String organizationId, OrganizationRelationshipRequest request, String actorUserId) {
        if (!organizationManagementMapper.organizationExists(organizationId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", "조직을 찾을 수 없습니다.", "organizationId");
        }
        if (!organizationManagementMapper.organizationExists(request.parentOrganizationId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "PARENT_ORGANIZATION_NOT_FOUND", "상위조직을 찾을 수 없습니다.", "parentOrganizationId");
        }

        OrganizationRelationship before = organizationManagementMapper.findRelationship(organizationId);
        // 기존 행을 수정하지 않고 비활성 이력으로 남긴 뒤 새 현재 관계를 추가한다.
        organizationManagementMapper.deactivateCurrentRelationship(organizationId);
        organizationManagementMapper.insertRelationship(
            "ORG-REL-" + UUID.randomUUID(),
            organizationId,
            request.parentOrganizationId(),
            request.effectiveStartDate(),
            request.effectiveEndDate()
        );

        organizationManagementMapper.insertChangeHistory(
            "CHANGE-" + UUID.randomUUID(),
            "organization_relationship",
            organizationId,
            serialize(toAuditValue(organizationId, before)),
            serialize(toAuditValue(organizationId, request)),
            actorUserId,
            request.reason()
        );
    }

    private LinkedHashMap<String, Object> toAuditValue(String organizationId, OrganizationRelationship relationship) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put("organizationId", organizationId);
        values.put("parentOrganizationId", relationship == null ? null : relationship.parentOrganizationId());
        values.put("effectiveStartDate", relationship == null ? null : relationship.effectiveStartDate());
        values.put("effectiveEndDate", relationship == null ? null : relationship.effectiveEndDate());
        return values;
    }

    private LinkedHashMap<String, Object> toAuditValue(String organizationId, OrganizationRelationshipRequest request) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put("organizationId", organizationId);
        values.put("parentOrganizationId", request.parentOrganizationId());
        values.put("effectiveStartDate", request.effectiveStartDate());
        values.put("effectiveEndDate", request.effectiveEndDate());
        return values;
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("조직 관계 변경 추적 값을 직렬화할 수 없습니다.", exception);
        }
    }

    public record OrganizationRow(
        String organizationId,
        String organizationCode,
        String organizationName,
        String organizationType,
        String parentOrganizationId,
        String parentOrganizationCode,
        String parentOrganizationName,
        LocalDate effectiveStartDate,
        LocalDate effectiveEndDate
    ) {
    }

    public record OrganizationRelationship(
        String organizationRelationshipId,
        String organizationId,
        String parentOrganizationId,
        LocalDate effectiveStartDate,
        LocalDate effectiveEndDate,
        String status
    ) {
    }

    public record OrganizationNode(String organizationId, String organizationCode, String organizationName) {
    }

    public record OrganizationSummary(
        String organizationId,
        String organizationCode,
        String organizationName,
        String organizationType,
        String parentOrganizationId,
        String parentOrganizationCode,
        String parentOrganizationName,
        LocalDate effectiveStartDate,
        LocalDate effectiveEndDate,
        List<OrganizationNode> children
    ) {
    }
}
