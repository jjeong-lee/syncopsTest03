package kr.ac.knue.facultyassessment.detailcodeusage;

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
public class DetailCodeUsageService {

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(20, 50, 100);

    private final DetailCodeUsageMapper detailCodeUsageMapper;
    private final UsagePeriodPolicy usagePeriodPolicy;
    private final ObjectMapper objectMapper;

    public DetailCodeUsageService(
        DetailCodeUsageMapper detailCodeUsageMapper,
        UsagePeriodPolicy usagePeriodPolicy,
        ObjectMapper objectMapper
    ) {
        this.detailCodeUsageMapper = detailCodeUsageMapper;
        this.usagePeriodPolicy = usagePeriodPolicy;
        this.objectMapper = objectMapper;
    }

    public List<DetailCodeUsageSetting> findDetailCodeUsageSettings(DetailCodeUsageSearchCriteria criteria) {
        if (criteria.page() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "페이지는 0 이상이어야 합니다.", "page");
        }
        if (!ALLOWED_PAGE_SIZES.contains(criteria.size())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE_SIZE", "목록 건수는 20, 50, 100 중 하나여야 합니다.", "size");
        }
        if (criteria.useYn() != null && !criteria.useYn().isBlank() && !isUseYn(criteria.useYn())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_USE_YN", "사용여부는 Y 또는 N이어야 합니다.", "useYn");
        }
        return detailCodeUsageMapper.findDetailCodeUsageSettings(criteria);
    }

    @Transactional
    public DetailCodeUsageSetting saveDetailCodeUsageSetting(
        DetailCodeUsageSettingRequest request,
        String actorUserId
    ) {
        if (!isUseYn(request.useYn())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_USE_YN", "사용여부는 Y 또는 N이어야 합니다.", "useYn");
        }
        usagePeriodPolicy.validateDetailCodeEffectivePeriod(
            request.effectiveStartDate(),
            request.effectiveEndDate()
        );
        DetailCodeUsageSetting before = detailCodeUsageMapper.findDetailCodeUsageSetting(
            request.groupId(),
            request.codeValue()
        );
        if (before == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DETAIL_CODE_NOT_FOUND", "상세코드를 찾을 수 없습니다.", "codeValue");
        }
        detailCodeUsageMapper.updateDetailCodeUsage(request, actorUserId);
        DetailCodeUsageSetting after = detailCodeUsageMapper.findDetailCodeUsageSetting(
            request.groupId(),
            request.codeValue()
        );
        detailCodeUsageMapper.insertChangeHistory(
            "CHANGE-" + UUID.randomUUID(),
            before.groupId() + ":" + before.codeValue(),
            serialize(before),
            serialize(after),
            actorUserId
        );
        return after;
    }

    private boolean isUseYn(String value) {
        return "Y".equals(value) || "N".equals(value);
    }

    private String serialize(DetailCodeUsageSetting value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("코드 사용 설정 변경값을 직렬화할 수 없습니다.", exception);
        }
    }
}
