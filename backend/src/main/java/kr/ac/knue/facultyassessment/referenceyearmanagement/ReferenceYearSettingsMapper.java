package kr.ac.knue.facultyassessment.referenceyearmanagement;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReferenceYearSettingsMapper {
    List<ReferenceYearSetting> findReferenceYearSettings(@Param("criteria") ReferenceYearSettingSearchCriteria criteria);
    ReferenceYearSetting findReferenceYearSetting(@Param("targetYear") Integer targetYear);
    void saveConfiguration(@Param("settingId") String settingId, @Param("request") ReferenceYearSettingRequest request, @Param("actorUserId") String actorUserId);
    void saveTargetSetting(@Param("settingId") String settingId, @Param("request") ReferenceYearSettingRequest request, @Param("actorUserId") String actorUserId);
    void insertChangeHistory(@Param("changeHistoryId") String changeHistoryId, @Param("entityId") String entityId, @Param("beforeValue") String beforeValue, @Param("afterValue") String afterValue, @Param("actorUserId") String actorUserId);
}
