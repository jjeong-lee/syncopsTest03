package kr.ac.knue.facultyassessment.settings;

import java.util.Map;
import java.util.stream.Collectors;
import kr.ac.knue.facultyassessment.common.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class CommonSettingsService {

    private static final String SESSION_IDLE_MINUTES = "SESSION_IDLE_MINUTES";
    private static final String PAGE_SIZE = "PAGE_SIZE";
    private static final String DEFAULT_SEARCH_PERIOD_DAYS = "DEFAULT_SEARCH_PERIOD_DAYS";
    private static final String BULK_QUERY_THRESHOLD = "BULK_QUERY_THRESHOLD";
    private static final String LONG_RUNNING_WORK_NOTICE_SECONDS = "LONG_RUNNING_WORK_NOTICE_SECONDS";
    private static final String CURRENT_EVALUATION_YEAR = "CURRENT_EVALUATION_YEAR";
    private static final String DEFAULT_SEARCH_YEAR = "DEFAULT_SEARCH_YEAR";
    private static final String REFERENCE_DATA_COPY_YN = "REFERENCE_DATA_COPY_YN";
    private static final String INITIALIZATION_YN = "INITIALIZATION_YN";

    private final CommonSettingsMapper commonSettingsMapper;

    public CommonSettingsService(CommonSettingsMapper commonSettingsMapper) {
        this.commonSettingsMapper = commonSettingsMapper;
    }

    public CommonSettings findCommonSettings() {
        Map<String, String> settings = commonSettingsMapper.findCommonSettings().stream()
            .collect(Collectors.toMap(CommonSettingsMapper.SettingRow::settingKey, CommonSettingsMapper.SettingRow::settingValue));
        if (!settings.keySet().containsAll(java.util.List.of(
            SESSION_IDLE_MINUTES, PAGE_SIZE, DEFAULT_SEARCH_PERIOD_DAYS, BULK_QUERY_THRESHOLD, LONG_RUNNING_WORK_NOTICE_SECONDS
        ))) {
            return null;
        }
        return new CommonSettings(
            Integer.parseInt(settings.get(SESSION_IDLE_MINUTES)),
            Integer.parseInt(settings.get(PAGE_SIZE)),
            Integer.parseInt(settings.get(DEFAULT_SEARCH_PERIOD_DAYS)),
            Integer.parseInt(settings.get(BULK_QUERY_THRESHOLD)),
            Integer.parseInt(settings.get(LONG_RUNNING_WORK_NOTICE_SECONDS))
        );
    }

    public ReferenceYearSettings findReferenceYearSettings() {
        Map<String, CommonSettingsMapper.SettingRow> settings = commonSettingsMapper.findReferenceYearSettings().stream()
            .collect(Collectors.toMap(CommonSettingsMapper.SettingRow::settingKey, setting -> setting));
        if (!settings.keySet().containsAll(java.util.List.of(
            CURRENT_EVALUATION_YEAR, DEFAULT_SEARCH_YEAR, REFERENCE_DATA_COPY_YN, INITIALIZATION_YN
        ))) {
            return null;
        }
        CommonSettingsMapper.SettingRow copySetting = settings.get(REFERENCE_DATA_COPY_YN);
        CommonSettingsMapper.SettingRow initializationSetting = settings.get(INITIALIZATION_YN);
        if (copySetting.targetYear() == null || !copySetting.targetYear().equals(initializationSetting.targetYear())) {
            return null;
        }
        return new ReferenceYearSettings(
            Integer.parseInt(settings.get(CURRENT_EVALUATION_YEAR).settingValue()),
            Integer.parseInt(settings.get(DEFAULT_SEARCH_YEAR).settingValue()),
            copySetting.targetYear(),
            copySetting.settingValue(),
            initializationSetting.settingValue()
        );
    }

    @Transactional
    public void saveCommonSettings(CommonSettingsRequest request, String actorUserId) {
        validateRange(request.sessionIdleMinutes(), 1, 1440, "sessionIdleMinutes", "세션 유휴시간은 1~1440분이어야 합니다.");
        if (!java.util.Set.of(20, 50, 100).contains(request.pageSize())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE_SIZE", "페이지당 조회건수는 20, 50, 100만 가능합니다.", "pageSize");
        }
        validateRange(request.defaultSearchPeriodDays(), 1, 365, "defaultSearchPeriodDays", "기본 검색기간은 1~365일이어야 합니다.");
        validateRange(request.bulkQueryThreshold(), 1, 100000, "bulkQueryThreshold", "대량조회 기준건수는 1~100000건이어야 합니다.");
        validateRange(request.longRunningWorkNoticeSeconds(), 1, 3600, "longRunningWorkNoticeSeconds", "장시간작업 안내 기준은 1~3600초여야 합니다.");

        save(SESSION_IDLE_MINUTES, request.sessionIdleMinutes(), actorUserId);
        save(PAGE_SIZE, request.pageSize(), actorUserId);
        save(DEFAULT_SEARCH_PERIOD_DAYS, request.defaultSearchPeriodDays(), actorUserId);
        save(BULK_QUERY_THRESHOLD, request.bulkQueryThreshold(), actorUserId);
        save(LONG_RUNNING_WORK_NOTICE_SECONDS, request.longRunningWorkNoticeSeconds(), actorUserId);
    }

    @Transactional
    public void saveReferenceYearSettings(ReferenceYearSettingsRequest request, String actorUserId) {
        validateUseYn(request.referenceDataCopyYn(), "referenceDataCopyYn", "기준정보 복사 여부는 Y 또는 N이어야 합니다.");
        validateUseYn(request.initializationYn(), "initializationYn", "초기화 여부는 Y 또는 N이어야 합니다.");

        save(CURRENT_EVALUATION_YEAR, request.currentEvaluationYear(), actorUserId);
        save(DEFAULT_SEARCH_YEAR, request.defaultSearchYear(), actorUserId);
        commonSettingsMapper.upsertSettingForTargetYear(
            REFERENCE_DATA_COPY_YN, request.referenceDataCopyYn(), request.targetYear(), actorUserId
        );
        commonSettingsMapper.upsertSettingForTargetYear(
            INITIALIZATION_YN, request.initializationYn(), request.targetYear(), actorUserId
        );
    }

    private void validateRange(int value, int minimum, int maximum, String field, String message) {
        if (value < minimum || value > maximum) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SETTING_VALUE", message, field);
        }
    }

    private void validateUseYn(String value, String field, String message) {
        if (!java.util.Set.of("Y", "N").contains(value)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SETTING_VALUE", message, field);
        }
    }

    private void save(String settingKey, int settingValue, String actorUserId) {
        commonSettingsMapper.upsertSetting(settingKey, Integer.toString(settingValue), actorUserId);
    }
}
