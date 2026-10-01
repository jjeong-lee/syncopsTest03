package kr.ac.knue.facultyassessment.settings;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommonSettingsMapper {

    List<SettingRow> findCommonSettings();

    List<SettingRow> findReferenceYearSettings();

    void upsertSetting(
        @Param("settingKey") String settingKey,
        @Param("settingValue") String settingValue,
        @Param("actorUserId") String actorUserId
    );

    void upsertSettingForTargetYear(
        @Param("settingKey") String settingKey,
        @Param("settingValue") String settingValue,
        @Param("targetYear") int targetYear,
        @Param("actorUserId") String actorUserId
    );

    record SettingRow(String settingKey, String settingValue, Integer targetYear) {
    }
}
