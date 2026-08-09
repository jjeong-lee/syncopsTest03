package kr.ac.knue.facultyassessment.roles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class RoleManagementService {

    private final RoleManagementMapper roleManagementMapper;
    private final ObjectMapper objectMapper;

    public RoleManagementService(RoleManagementMapper roleManagementMapper, ObjectMapper objectMapper) {
        this.roleManagementMapper = roleManagementMapper;
        this.objectMapper = objectMapper;
    }

    public List<RoleSummary> findRoles() {
        return roleManagementMapper.findRoles();
    }

    @Transactional
    public void saveRole(RoleRequest request, String actorUserId) {
        RoleSummary before = roleManagementMapper.findRole(request.roleCode());
        if (before == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ROLE_CODE_IMMUTABLE", "등록된 역할코드만 변경할 수 있습니다.", "roleCode");
        }

        roleManagementMapper.updateRole(
            before.roleCode(),
            request.roleName(),
            request.purpose(),
            request.assignmentCriteria(),
            request.defaultDataScope()
        );
        roleManagementMapper.insertChangeHistory(
            "CHANGE-" + UUID.randomUUID(),
            "role",
            before.roleCode(),
            serialize(before),
            serialize(new RoleSummary(
                before.roleCode(),
                request.roleName(),
                request.purpose(),
                request.assignmentCriteria(),
                request.defaultDataScope()
            )),
            actorUserId,
            request.reason()
        );
    }

    private String serialize(RoleSummary role) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put("roleCode", role.roleCode());
        values.put("roleName", role.roleName());
        values.put("purpose", role.purpose());
        values.put("assignmentCriteria", role.assignmentCriteria());
        values.put("defaultDataScope", role.defaultDataScope());
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("역할 변경 추적 값을 직렬화할 수 없습니다.", exception);
        }
    }

    public record RoleSummary(
        String roleCode,
        String roleName,
        String purpose,
        String assignmentCriteria,
        String defaultDataScope
    ) {
    }
}
