package kr.ac.knue.facultyassessment.menupermissions;

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
public class MenuPermissionManagementService {

    private final MenuPermissionManagementMapper menuPermissionManagementMapper;
    private final ObjectMapper objectMapper;

    public MenuPermissionManagementService(MenuPermissionManagementMapper menuPermissionManagementMapper, ObjectMapper objectMapper) {
        this.menuPermissionManagementMapper = menuPermissionManagementMapper;
        this.objectMapper = objectMapper;
    }

    public List<MenuPermissionSummary> findMenuPermissions(MenuPermissionSearchCriteria criteria) {
        return menuPermissionManagementMapper.findMenuPermissions(criteria);
    }

    @Transactional
    public void saveMenuPermission(MenuPermissionRequest request, String actorUserId) {
        if (!menuPermissionManagementMapper.menuExists(request.menuId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MENU_NOT_FOUND", "메뉴를 찾을 수 없습니다.", "menuId");
        }
        if (!menuPermissionManagementMapper.subjectExists(request.subjectType(), request.subjectId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SUBJECT_NOT_FOUND", "권한 대상을 찾을 수 없습니다.", "subjectId");
        }

        MenuPermissionSummary before = menuPermissionManagementMapper.findMenuPermission(
            request.subjectType(), request.subjectId(), request.menuId()
        );
        menuPermissionManagementMapper.upsertMenuPermission(
            before == null ? "PERMISSION-" + UUID.randomUUID() : before.menuPermissionId(),
            request.subjectType(),
            request.subjectId(),
            request.menuId(),
            request.accessAllowed()
        );
        MenuPermissionSummary after = menuPermissionManagementMapper.findMenuPermission(
            request.subjectType(), request.subjectId(), request.menuId()
        );
        menuPermissionManagementMapper.insertChangeHistory(
            "CHANGE-" + UUID.randomUUID(),
            "menu_permission",
            request.subjectType() + ":" + request.subjectId() + ":" + request.menuId(),
            serialize(before),
            serialize(after),
            actorUserId,
            request.reason()
        );
    }

    private String serialize(MenuPermissionSummary value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("메뉴 권한 변경 추적 값을 직렬화할 수 없습니다.", exception);
        }
    }

    public record MenuPermissionSummary(
        String menuPermissionId,
        String subjectType,
        String subjectId,
        String menuId,
        String majorMenuName,
        String middleMenuName,
        String screenName,
        String accessAllowed
    ) {
    }
}
