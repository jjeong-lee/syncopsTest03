package kr.ac.knue.facultyassessment.organizations;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrganizationManagementMapper {

    List<OrganizationManagementService.OrganizationRow> findOrganizations(@Param("criteria") OrganizationSearchCriteria criteria);

    OrganizationManagementService.OrganizationRelationship findRelationship(@Param("organizationId") String organizationId);

    boolean organizationExists(@Param("organizationId") String organizationId);

    int updateRelationship(
        @Param("organizationId") String organizationId,
        @Param("parentOrganizationId") String parentOrganizationId,
        @Param("effectiveStartDate") java.time.LocalDate effectiveStartDate,
        @Param("effectiveEndDate") java.time.LocalDate effectiveEndDate
    );

    void insertRelationship(
        @Param("organizationRelationshipId") String organizationRelationshipId,
        @Param("organizationId") String organizationId,
        @Param("parentOrganizationId") String parentOrganizationId,
        @Param("effectiveStartDate") java.time.LocalDate effectiveStartDate,
        @Param("effectiveEndDate") java.time.LocalDate effectiveEndDate
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
