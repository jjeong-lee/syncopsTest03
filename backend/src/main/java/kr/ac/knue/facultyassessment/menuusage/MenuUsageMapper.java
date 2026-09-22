package kr.ac.knue.facultyassessment.menuusage;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MenuUsageMapper {

    List<MenuUsageSetting> findMenuUsageSettings(@Param("criteria") MenuUsageSearchCriteria criteria);

    MenuUsageSetting findMenuUsageSetting(@Param("menuId") String menuId);

    void updateMenuUsage(@Param("request") MenuUsageSettingRequest request, @Param("actorUserId") String actorUserId);

    void insertChangeHistory(
        @Param("changeHistoryId") String changeHistoryId,
        @Param("entityId") String entityId,
        @Param("beforeValue") String beforeValue,
        @Param("afterValue") String afterValue,
        @Param("actorUserId") String actorUserId
    );
}
