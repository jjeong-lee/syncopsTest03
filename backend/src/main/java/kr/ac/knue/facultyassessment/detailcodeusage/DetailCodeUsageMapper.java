package kr.ac.knue.facultyassessment.detailcodeusage;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DetailCodeUsageMapper {

    List<DetailCodeUsageSetting> findDetailCodeUsageSettings(
        @Param("criteria") DetailCodeUsageSearchCriteria criteria
    );

    DetailCodeUsageSetting findDetailCodeUsageSetting(
        @Param("groupId") String groupId,
        @Param("codeValue") String codeValue
    );

    void updateDetailCodeUsage(
        @Param("request") DetailCodeUsageSettingRequest request,
        @Param("actorUserId") String actorUserId
    );

    void insertChangeHistory(
        @Param("changeHistoryId") String changeHistoryId,
        @Param("entityId") String entityId,
        @Param("beforeValue") String beforeValue,
        @Param("afterValue") String afterValue,
        @Param("actorUserId") String actorUserId
    );
}
