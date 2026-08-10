package kr.ac.knue.facultyassessment.codegroups;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CodeGroupManagementMapper {

    List<CodeGroupManagementService.CodeGroupSummary> findCodeGroups(@Param("criteria") CodeGroupSearchCriteria criteria);

    CodeGroupManagementService.CodeGroupSummary findCodeGroupById(@Param("groupId") String groupId);

    void insertCodeGroup(@Param("request") CodeGroupRequest request, @Param("useYn") String useYn);

    void updateCodeGroup(@Param("request") CodeGroupRequest request, @Param("useYn") String useYn);

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
