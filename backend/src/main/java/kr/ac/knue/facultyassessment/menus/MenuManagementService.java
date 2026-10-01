package kr.ac.knue.facultyassessment.menus;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class MenuManagementService {

    private final MenuManagementMapper menuManagementMapper;
    private final ObjectMapper objectMapper;

    public MenuManagementService(MenuManagementMapper menuManagementMapper, ObjectMapper objectMapper) {
        this.menuManagementMapper = menuManagementMapper;
        this.objectMapper = objectMapper;
    }

    public List<MenuSummary> findMenus(MenuSearchCriteria criteria) {
        return menuManagementMapper.findMenus(criteria);
    }

    @Transactional
    public void saveMenuStructure(MenuRequest request, String actorUserId) {
        validateUsagePeriod(request);
        MenuSummary before = menuManagementMapper.findMenuByNameAndScreen(request.menuName(), request.screenId());
        if (request.parentMenuId() != null && !request.parentMenuId().isBlank() && !menuManagementMapper.menuExists(request.parentMenuId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PARENT_MENU_NOT_FOUND", "부모메뉴를 찾을 수 없습니다.", "parentMenuId");
        }

        if (before == null) {
            String menuId = "MENU-" + UUID.randomUUID();
            menuManagementMapper.insertMenu(menuId, request, normalizedUseYn(request.useYn()));
            recordChange(null, menuManagementMapper.findMenuByMenuId(menuId), actorUserId, request.reason());
            return;
        }

        menuManagementMapper.updateMenu(before.menuId(), request, normalizedUseYn(request.useYn(), before.useYn()));
        recordChange(before, menuManagementMapper.findMenuByMenuId(before.menuId()), actorUserId, request.reason());
    }

    @Transactional
    public void reorderMenu(MenuOrderRequest request, String actorUserId) {
        MenuSummary before = menuManagementMapper.findMenuByMenuId(request.menuId());
        if (before == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MENU_NOT_FOUND", "메뉴를 찾을 수 없습니다.", "menuId");
        }

        menuManagementMapper.updateDisplayOrder(before.menuId(), request.displayOrder());
        recordChange(before, menuManagementMapper.findMenuByMenuId(before.menuId()), actorUserId, request.reason());
    }

    private void recordChange(MenuSummary before, MenuSummary after, String actorUserId, String reason) {
        menuManagementMapper.insertChangeHistory(
            "CHANGE-" + UUID.randomUUID(),
            "menu",
            after.menuId(),
            serialize(before),
            serialize(after),
            actorUserId,
            reason
        );
    }

    private String serialize(MenuSummary value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("메뉴 변경 추적 값을 직렬화할 수 없습니다.", exception);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private void validateUsagePeriod(MenuRequest request) {
        boolean usageManagement = "SCR-MENU-USAGE-MANAGEMENT".equals(request.screenId());
        if (usageManagement && request.exposureStartAt() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EXPOSURE_START_REQUIRED", "노출 시작일시는 필수입니다.", "exposureStartAt");
        }
        if (request.exposureStartAt() != null && request.exposureEndAt() != null
            && request.exposureStartAt().isAfter(request.exposureEndAt())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EXPOSURE_PERIOD", "노출 종료일시는 시작일시보다 빠를 수 없습니다.", "exposureEndAt");
        }
    }

    private String normalizedUseYn(String useYn) {
        return normalizedUseYn(useYn, "Y");
    }

    private String normalizedUseYn(String useYn, String fallback) {
        return useYn == null || useYn.isBlank() ? fallback : useYn;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MenuSummary(
        String menuId,
        String menuName,
        String parentMenuId,
        Integer displayOrder,
        String screenId,
        String url,
        String icon,
        String businessCategory,
        String description,
        String useYn,
        java.time.OffsetDateTime exposureStartAt,
        java.time.OffsetDateTime exposureEndAt
    ) {
    }
}
