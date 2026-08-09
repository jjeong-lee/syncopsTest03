package kr.ac.knue.facultyassessment.menupermissions;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MenuPermissionManagementMapper {

    List<MenuPermissionManagementService.MenuPermissionSummary> findMenuPermissions(
        @Param("criteria") MenuPermissionSearchCriteria criteria
    );

    MenuPermissionManagementService.MenuPermissionSummary findMenuPermission(
        @Param("subjectType") String subjectType,
        @Param("subjectId") String subjectId,
        @Param("menuId") String menuId
    );

    boolean menuExists(@Param("menuId") String menuId);

    boolean subjectExists(@Param("subjectType") String subjectType, @Param("subjectId") String subjectId);

    void upsertMenuPermission(
        @Param("menuPermissionId") String menuPermissionId,
        @Param("subjectType") String subjectType,
        @Param("subjectId") String subjectId,
        @Param("menuId") String menuId,
        @Param("accessAllowed") String accessAllowed
    );

    void insertChangeHistory(
        @Param("changeHistoryId") String changeHistoryId,
        @Param("entityName") String entityName,
        @Param("entityId") String entityId,
        @Param("beforeValue") String beforeValue,
        @Param("afterValue") String afterValue,
        @Param("actorUserId") String actorUserId,
        @Param("reason") String reason
    );
}
