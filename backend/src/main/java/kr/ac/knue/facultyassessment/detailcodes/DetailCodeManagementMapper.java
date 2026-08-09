package kr.ac.knue.facultyassessment.detailcodes;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DetailCodeManagementMapper {

    List<DetailCodeManagementService.DetailCodeRow> findDetailCodes(@Param("criteria") DetailCodeSearchCriteria criteria);

    DetailCodeManagementService.DetailCodeRow findDetailCodeByGroupAndCode(
        @Param("groupId") String groupId,
        @Param("codeValue") String codeValue
    );

    void insertDetailCode(
        @Param("detailCodeId") String detailCodeId,
        @Param("groupId") String groupId,
        @Param("request") DetailCodeRequest request,
        @Param("additionalAttributes") String additionalAttributes,
        @Param("useYn") String useYn
    );

    void updateDetailCode(
        @Param("detailCodeId") String detailCodeId,
        @Param("request") DetailCodeRequest request,
        @Param("additionalAttributes") String additionalAttributes,
        @Param("useYn") String useYn
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
