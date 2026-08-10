package kr.ac.knue.facultyassessment.users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import kr.ac.knue.facultyassessment.common.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class UserManagementService {

    private final UserManagementMapper userManagementMapper;
    private final ObjectMapper objectMapper;

    public UserManagementService(UserManagementMapper userManagementMapper, ObjectMapper objectMapper) {
        this.userManagementMapper = userManagementMapper;
        this.objectMapper = objectMapper;
    }

    public List<UserSummary> findUsers(UserSearchCriteria criteria) {
        return userManagementMapper.findUsers(criteria).stream()
            .map(user -> user.withRoleCodes(userManagementMapper.findRoleCodesByUserId(user.userId())))
            .toList();
    }

    @Transactional
    public void updateSettings(String userId, UserSettingsRequest request, String actorUserId) {
        LocalUserSettings before = userManagementMapper.findLocalSettings(userId);
        if (before == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", "userId");
        }
        List<String> beforeRoleCodes = userManagementMapper.findRoleCodesByUserId(userId);

        userManagementMapper.updateUseYn(userId, request.useYn());
        userManagementMapper.deactivateRolesByUserId(userId);
        request.roleCodes().forEach(roleCode -> userManagementMapper.insertUserRole(
            "USER-ROLE-" + UUID.randomUUID(), userId, roleCode, actorUserId
        ));

        Map<String, Object> beforeAuditValue = Map.of(
            "userId", before.userId(),
            "useYn", before.useYn(),
            "roleCodes", beforeRoleCodes
        );
        Map<String, Object> afterAuditValue = Map.of(
            "userId", userId,
            "useYn", request.useYn(),
            "roleCodes", request.roleCodes()
        );
        userManagementMapper.insertChangeHistory(
            "CHANGE-" + UUID.randomUUID(),
            "user_account",
            userId,
            serialize(beforeAuditValue),
            serialize(afterAuditValue),
            actorUserId,
            request.reason()
        );
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("변경 추적 값을 직렬화할 수 없습니다.", exception);
        }
    }

    public record UserSummary(
        String userId,
        String personnelNo,
        String name,
        String organization,
        String position,
        String employmentStatus,
        List<String> roleCodes,
        String useYn,
        String positionTitle,
        java.time.LocalDate retirementDate,
        OffsetDateTime lastSyncedAt
    ) {
        UserSummary withRoleCodes(List<String> updatedRoleCodes) {
            return new UserSummary(userId, personnelNo, name, organization, position, employmentStatus, updatedRoleCodes, useYn,
                positionTitle, retirementDate, lastSyncedAt);
        }
    }

    public record LocalUserSettings(String userId, String useYn) {
    }
}
