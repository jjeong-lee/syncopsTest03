package kr.ac.knue.facultyassessment.commonenvironmentsettings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import kr.ac.knue.facultyassessment.common.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class CommonEnvironmentSettingsService {
    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(20, 50, 100);
    private final CommonEnvironmentSettingsMapper mapper;
    private final ObjectMapper objectMapper;

    public CommonEnvironmentSettingsService(CommonEnvironmentSettingsMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    public List<CommonEnvironmentSetting> findCommonEnvironmentSettings(CommonEnvironmentSettingSearchCriteria criteria) {
        if (criteria.page() < 0) throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "페이지는 0 이상이어야 합니다.", "page");
        if (!ALLOWED_PAGE_SIZES.contains(criteria.size())) throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE_SIZE", "목록 건수는 20, 50, 100 중 하나여야 합니다.", "size");
        return mapper.findCommonEnvironmentSettings(criteria);
    }

    @Transactional
    public CommonEnvironmentSetting saveCommonEnvironmentSetting(CommonEnvironmentSettingRequest request, String actorUserId) {
        validate(request);
        CommonEnvironmentSetting before = mapper.findCommonEnvironmentSetting(request.settingKey());
        mapper.saveCommonEnvironmentSetting("COMMON-ENVIRONMENT-" + UUID.randomUUID(), request, actorUserId);
        CommonEnvironmentSetting after = mapper.findCommonEnvironmentSetting(request.settingKey());
        mapper.insertChangeHistory("CHANGE-" + UUID.randomUUID(), request.settingKey(), serialize(before), serialize(after), actorUserId);
        return after;
    }

    private void validate(CommonEnvironmentSettingRequest request) {
        if (!"page_size".equals(request.settingKey())) return;
        try {
            if (ALLOWED_PAGE_SIZES.contains(Integer.parseInt(request.settingValue()))) return;
        } catch (NumberFormatException ignored) {
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE_SIZE_SETTING", "페이지당 조회건수는 20, 50, 100 중 하나여야 합니다.", "settingValue");
    }

    private String serialize(CommonEnvironmentSetting value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("공통 환경설정 변경값을 직렬화할 수 없습니다.", exception);
        }
    }
}
