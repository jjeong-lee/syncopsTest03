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

    @Transactional
    public void saveRelationship(String organizationId, OrganizationRelationshipRequest request, String actorUserId) {
        if (!organizationManagementMapper.organizationExists(organizationId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "ORGANIZATION_NOT_FOUND", "조직을 찾을 수 없습니다.", "organizationId");
        }
        if (!organizationManagementMapper.organizationExists(request.parentOrganizationId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "PARENT_ORGANIZATION_NOT_FOUND", "상위조직을 찾을 수 없습니다.", "parentOrganizationId");
        }

        OrganizationRelationship before = organizationManagementMapper.findRelationship(organizationId);
        int updated = organizationManagementMapper.updateRelationship(
            organizationId,
            request.parentOrganizationId(),
            request.effectiveStartDate(),
            request.effectiveEndDate()
        );
        if (updated == 0) {
            organizationManagementMapper.insertRelationship(
                "ORG-REL-" + UUID.randomUUID(),
                organizationId,
                request.parentOrganizationId(),
                request.effectiveStartDate(),
                request.effectiveEndDate()
            );
        }

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
        LocalDate effectiveEndDate
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
