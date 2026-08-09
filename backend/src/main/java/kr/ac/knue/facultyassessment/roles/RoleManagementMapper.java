package kr.ac.knue.facultyassessment.roles;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoleManagementMapper {

    List<RoleManagementService.RoleSummary> findRoles();

    RoleManagementService.RoleSummary findRole(@Param("roleCode") String roleCode);

    int updateRole(
        @Param("roleCode") String roleCode,
        @Param("roleName") String roleName,
        @Param("purpose") String purpose,
        @Param("assignmentCriteria") String assignmentCriteria,
        @Param("defaultDataScope") String defaultDataScope
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
