package kr.ac.knue.facultyassessment.commonenvironmentsettings;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommonEnvironmentSettingsMapper {
    List<CommonEnvironmentSetting> findCommonEnvironmentSettings(@Param("criteria") CommonEnvironmentSettingSearchCriteria criteria);
    CommonEnvironmentSetting findCommonEnvironmentSetting(@Param("settingKey") String settingKey);
    void saveCommonEnvironmentSetting(@Param("settingId") String settingId, @Param("request") CommonEnvironmentSettingRequest request, @Param("actorUserId") String actorUserId);
    void insertChangeHistory(@Param("changeHistoryId") String changeHistoryId, @Param("entityId") String entityId, @Param("beforeValue") String beforeValue, @Param("afterValue") String afterValue, @Param("actorUserId") String actorUserId);
}
