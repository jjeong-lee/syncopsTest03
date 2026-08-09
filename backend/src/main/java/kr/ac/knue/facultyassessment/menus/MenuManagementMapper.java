package kr.ac.knue.facultyassessment.menus;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MenuManagementMapper {

    List<MenuManagementService.MenuSummary> findMenus(@Param("criteria") MenuSearchCriteria criteria);

    MenuManagementService.MenuSummary findMenuByMenuId(@Param("menuId") String menuId);

    MenuManagementService.MenuSummary findMenuByNameAndScreen(
        @Param("menuName") String menuName,
        @Param("screenId") String screenId
    );

    boolean menuExists(@Param("menuId") String menuId);

    void updateMenu(
        @Param("menuId") String menuId,
        @Param("request") MenuRequest request,
        @Param("useYn") String useYn
    );

    void insertMenu(
        @Param("menuId") String menuId,
        @Param("request") MenuRequest request,
        @Param("useYn") String useYn
    );

    void updateDisplayOrder(@Param("menuId") String menuId, @Param("displayOrder") Integer displayOrder);

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
