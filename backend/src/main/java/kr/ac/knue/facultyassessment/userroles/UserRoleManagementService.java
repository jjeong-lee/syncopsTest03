package kr.ac.knue.facultyassessment.userroles;

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
public class UserRoleManagementService {

    private final UserRoleManagementMapper userRoleManagementMapper;
    private final ObjectMapper objectMapper;

    public UserRoleManagementService(UserRoleManagementMapper userRoleManagementMapper, ObjectMapper objectMapper) {
        this.userRoleManagementMapper = userRoleManagementMapper;
        this.objectMapper = objectMapper;
    }

    public List<UserRoleSummary> findActiveUserRoles(String userId) {
        if (!userRoleManagementMapper.userExists(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", "userId");
        }
        return userRoleManagementMapper.findActiveUserRoles(userId);
    }

    @Transactional
    public void saveUserRole(String userId, UserRoleRequest request, String actorUserId) {
        if (!userRoleManagementMapper.userExists(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", "userId");
        }
        if (!userRoleManagementMapper.userExists(request.approvalUserId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "APPROVER_NOT_FOUND", "승인자를 찾을 수 없습니다.", "approvalUserId");
        }
        if (!userRoleManagementMapper.roleExists(request.roleCode())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ROLE_NOT_FOUND", "정의되지 않은 역할코드입니다.", "roleCode");
        }

        UserRoleSummary before = userRoleManagementMapper.findActiveUserRole(userId, request.roleCode());
        if (before == null) {
            userRoleManagementMapper.insertUserRole(
                "USER-ROLE-" + UUID.randomUUID(),
                userId,
                request.roleCode(),
                request.approvalUserId(),
                request.effectiveStartDate(),
                request.effectiveEndDate()
            );
        } else {
            userRoleManagementMapper.updateUserRole(
                before.userRoleId(),
                request.approvalUserId(),
                request.effectiveStartDate(),
                request.effectiveEndDate()
            );
        }

        userRoleManagementMapper.insertChangeHistory(
            "CHANGE-" + UUID.randomUUID(),
            "user_role",
            userId + ":" + request.roleCode(),
            serialize(before),
            serialize(new UserRoleSummary(
                before == null ? null : before.userRoleId(),
                request.roleCode(),
                request.approvalUserId(),
                request.effectiveStartDate(),
                request.effectiveEndDate(),
                before == null ? "MANUAL" : before.assignmentType(),
                "ACTIVE"
            )),
            actorUserId,
            request.reason()
        );
    }

    private String serialize(UserRoleSummary userRole) {
        if (userRole == null) {
            return null;
        }
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put("userRoleId", userRole.userRoleId());
        values.put("roleCode", userRole.roleCode());
        values.put("approvalUserId", userRole.approvalUserId());
        values.put("effectiveStartDate", userRole.effectiveStartDate());
        values.put("effectiveEndDate", userRole.effectiveEndDate());
        values.put("assignmentType", userRole.assignmentType());
        values.put("status", userRole.status());
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("사용자 역할 변경 추적 값을 직렬화할 수 없습니다.", exception);
        }
    }

    public record UserRoleSummary(
        String userRoleId,
        String roleCode,
        String approvalUserId,
        LocalDate effectiveStartDate,
        LocalDate effectiveEndDate,
        String assignmentType,
        String status
    ) {
    }
}
