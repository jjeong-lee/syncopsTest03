package kr.ac.knue.facultyassessment.referenceyearmanagement;

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
public class ReferenceYearSettingsService {

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(20, 50, 100);
    private static final Set<String> ALLOWED_OPTIONS = Set.of("예", "아니오");

    private final ReferenceYearSettingsMapper referenceYearSettingsMapper;
    private final ObjectMapper objectMapper;

    public ReferenceYearSettingsService(ReferenceYearSettingsMapper referenceYearSettingsMapper, ObjectMapper objectMapper) {
        this.referenceYearSettingsMapper = referenceYearSettingsMapper;
        this.objectMapper = objectMapper;
    }

    public List<ReferenceYearSetting> findReferenceYearSettings(ReferenceYearSettingSearchCriteria criteria) {
        if (criteria.page() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "페이지는 0 이상이어야 합니다.", "page");
        }
        if (!ALLOWED_PAGE_SIZES.contains(criteria.size())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE_SIZE", "목록 건수는 20, 50, 100 중 하나여야 합니다.", "size");
        }
        return referenceYearSettingsMapper.findReferenceYearSettings(criteria);
    }

    @Transactional
    public ReferenceYearSetting saveReferenceYearSetting(ReferenceYearSettingRequest request, String actorUserId) {
        validateOptions(request);
        ReferenceYearSetting before = referenceYearSettingsMapper.findReferenceYearSetting(request.targetYear());
        referenceYearSettingsMapper.saveConfiguration("REFERENCE-YEAR-CONFIGURATION", request, actorUserId);
        referenceYearSettingsMapper.saveTargetSetting("REFERENCE-YEAR-TARGET-" + request.targetYear(), request, actorUserId);
        ReferenceYearSetting after = referenceYearSettingsMapper.findReferenceYearSetting(request.targetYear());
        referenceYearSettingsMapper.insertChangeHistory(
            "CHANGE-" + UUID.randomUUID(),
            String.valueOf(request.targetYear()),
            serialize(before),
            serialize(after),
            actorUserId
        );
        return after;
    }

    private void validateOptions(ReferenceYearSettingRequest request) {
        if (!ALLOWED_OPTIONS.contains(request.baselineCopyYn())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REFERENCE_YEAR_OPTION", "기준정보 복사 여부는 예 또는 아니오여야 합니다.", "baselineCopyYn");
        }
        if (!ALLOWED_OPTIONS.contains(request.initializationYn())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REFERENCE_YEAR_OPTION", "초기화 여부는 예 또는 아니오여야 합니다.", "initializationYn");
        }
    }

    private String serialize(ReferenceYearSetting value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("기준연도 설정 변경값을 직렬화할 수 없습니다.", exception);
        }
    }
}
