package kr.ac.knue.facultyassessment.userroles;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRoleManagementMapper {

    List<UserRoleManagementService.UserRoleSummary> findActiveUserRoles(@Param("userId") String userId);

    UserRoleManagementService.UserRoleSummary findCurrentUserRole(
        @Param("userId") String userId,
        @Param("roleCode") String roleCode
    );

    UserRoleManagementService.UserRoleSummary findCurrentUserRoleById(
        @Param("userId") String userId,
        @Param("userRoleId") String userRoleId
    );

    boolean userExists(@Param("userId") String userId);

    boolean roleExists(@Param("roleCode") String roleCode);

    int updateUserRole(
        @Param("userRoleId") String userRoleId,
        @Param("approvalUserId") String approvalUserId,
        @Param("effectiveStartDate") LocalDate effectiveStartDate,
        @Param("effectiveEndDate") LocalDate effectiveEndDate
    );

    int revokeUserRole(@Param("userRoleId") String userRoleId);

    void insertUserRole(
        @Param("userRoleId") String userRoleId,
        @Param("userId") String userId,
        @Param("roleCode") String roleCode,
        @Param("approvalUserId") String approvalUserId,
        @Param("effectiveStartDate") LocalDate effectiveStartDate,
        @Param("effectiveEndDate") LocalDate effectiveEndDate
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
