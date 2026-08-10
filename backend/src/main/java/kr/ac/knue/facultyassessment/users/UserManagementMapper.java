package kr.ac.knue.facultyassessment.users;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserManagementMapper {

    List<UserManagementService.UserSummary> findUsers(@Param("criteria") UserSearchCriteria criteria);

    List<String> findRoleCodesByUserId(@Param("userId") String userId);

    UserManagementService.LocalUserSettings findLocalSettings(@Param("userId") String userId);

    int updateUseYn(@Param("userId") String userId, @Param("useYn") String useYn);

    void deactivateRolesByUserId(@Param("userId") String userId);

    void insertUserRole(@Param("userRoleId") String userRoleId, @Param("userId") String userId, @Param("roleCode") String roleCode,
        @Param("actorUserId") String actorUserId);

    void insertChangeHistory(@Param("changeHistoryId") String changeHistoryId, @Param("entityName") String entityName,
        @Param("entityId") String entityId, @Param("beforeValue") String beforeValue, @Param("afterValue") String afterValue,
        @Param("actorUserId") String actorUserId, @Param("reason") String reason);
}
