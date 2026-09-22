package kr.ac.knue.facultyassessment.menuusage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import kr.ac.knue.facultyassessment.common.ApiException;
import kr.ac.knue.facultyassessment.common.UsagePeriodPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class MenuUsageService {

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(20, 50, 100);

    private final MenuUsageMapper menuUsageMapper;
    private final UsagePeriodPolicy usagePeriodPolicy;
    private final ObjectMapper objectMapper;

    public MenuUsageService(
        MenuUsageMapper menuUsageMapper,
        UsagePeriodPolicy usagePeriodPolicy,
        ObjectMapper objectMapper
    ) {
        this.menuUsageMapper = menuUsageMapper;
        this.usagePeriodPolicy = usagePeriodPolicy;
        this.objectMapper = objectMapper;
    }

    public List<MenuUsageSetting> findMenuUsageSettings(MenuUsageSearchCriteria criteria) {
        if (criteria.page() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "페이지는 0 이상이어야 합니다.", "page");
        }
        if (!ALLOWED_PAGE_SIZES.contains(criteria.size())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE_SIZE", "목록 건수는 20, 50, 100 중 하나여야 합니다.", "size");
        }
        if (criteria.useYn() != null && !criteria.useYn().isBlank() && !isUseYn(criteria.useYn())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_USE_YN", "사용여부는 Y 또는 N이어야 합니다.", "useYn");
        }
        return menuUsageMapper.findMenuUsageSettings(criteria);
    }

    @Transactional
    public MenuUsageSetting saveMenuUsageSetting(MenuUsageSettingRequest request, String actorUserId) {
        if (!isUseYn(request.useYn())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_USE_YN", "사용여부는 Y 또는 N이어야 합니다.", "useYn");
        }
        usagePeriodPolicy.validateMenuExposurePeriod(request.exposureStartAt(), request.exposureEndAt());
        MenuUsageSetting before = menuUsageMapper.findMenuUsageSetting(request.menuId());
        if (before == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MENU_NOT_FOUND", "메뉴를 찾을 수 없습니다.", "menuId");
        }
        menuUsageMapper.updateMenuUsage(request, actorUserId);
        MenuUsageSetting after = menuUsageMapper.findMenuUsageSetting(request.menuId());
        menuUsageMapper.insertChangeHistory(
            "CHANGE-" + UUID.randomUUID(),
            request.menuId(),
            serialize(before),
            serialize(after),
            actorUserId
        );
        return after;
    }

    private boolean isUseYn(String value) {
        return "Y".equals(value) || "N".equals(value);
    }

    private String serialize(MenuUsageSetting value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("메뉴 사용 설정 변경값을 직렬화할 수 없습니다.", exception);
        }
    }
}
