package kr.ac.knue.facultyassessment.assignments;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AssignmentManagementMapper {
    List<PositionAssignment> findPositionAssignments(@Param("criteria") PositionCriteria criteria);
    List<WorkAssignment> findWorkAssignments(@Param("criteria") WorkCriteria criteria);
    List<RoleDataScope> findRoleDataScopes(@Param("criteria") RoleDataScopeCriteria criteria);
    boolean userExists(@Param("userId") String userId);
    boolean organizationExists(@Param("organizationId") String organizationId);
    boolean organizationCodeExists(@Param("organizationCode") String organizationCode);
    boolean roleExists(@Param("roleCode") String roleCode);
    boolean hasPositionOverlap(@Param("request") PositionAssignmentRequest request);
    boolean hasWorkOverlap(@Param("request") WorkAssignmentRequest request);
    void insertPosition(@Param("id") String id, @Param("request") PositionAssignmentRequest request);
    void updatePosition(@Param("request") PositionAssignmentRequest request);
    void insertWork(@Param("id") String id, @Param("request") WorkAssignmentRequest request);
    void updateWork(@Param("request") WorkAssignmentRequest request);
    void insertRoleDataScope(@Param("id") String id, @Param("request") RoleDataScopeRequest request);
    void updateRoleDataScope(@Param("request") RoleDataScopeRequest request);

    record PositionCriteria(String positionCode, String userId, String organizationId, LocalDate referenceDate, Integer page, Integer size) {}
    record WorkCriteria(String organizationId, String userId, String workArea, LocalDate referenceDate, Integer page, Integer size) {}
    record RoleDataScopeCriteria(String roleCode, String dataScopeType, String organizationCode, String workArea, Integer page, Integer size) {}
    record PositionAssignment(String positionAssignmentId, String positionCode, String userId, String organizationId, LocalDate effectiveStartDate, LocalDate effectiveEndDate) {}
    record WorkAssignment(String workAssignmentId, String organizationId, String userId, String workArea, LocalDate effectiveStartDate, LocalDate effectiveEndDate, String dataScopeType, String processPermission) {}
    record RoleDataScope(String roleDataScopeId, String roleCode, String dataScopeType, String organizationCode, String workArea) {}
}
