package kr.ac.knue.facultyassessment.assignments;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import kr.ac.knue.facultyassessment.common.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class AssignmentManagementController {
    private static final MediaType XLSX = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private final AssignmentManagementService service;
    public AssignmentManagementController(AssignmentManagementService service) { this.service = service; }

    @GetMapping("/api/position-assignments") public ApiResponse<?> listPositions(@RequestParam(required=false) String positionCode, @RequestParam(required=false) String userId, @RequestParam(required=false) String organizationId, @RequestParam(required=false) LocalDate referenceDate, @RequestParam(defaultValue="0") Integer page, @RequestParam(defaultValue="20") Integer size) { return ApiResponse.success(service.findPositions(new AssignmentManagementMapper.PositionCriteria(positionCode,userId,organizationId,referenceDate,page * size,size))); }
    @PostMapping("/api/position-assignments") public ApiResponse<Void> savePosition(@Valid @RequestBody PositionAssignmentRequest request) { service.savePosition(request); return ApiResponse.success(null); }
    @GetMapping("/api/position-assignments/export") public ResponseEntity<byte[]> exportPositions(@RequestParam(required=false) String positionCode, @RequestParam(required=false) String userId, @RequestParam(required=false) String organizationId, @RequestParam(required=false) LocalDate referenceDate) { var rows=service.findPositions(new AssignmentManagementMapper.PositionCriteria(positionCode,userId,organizationId,referenceDate,null,null)).stream().map(item -> List.of(item.positionCode(),item.userId(),item.organizationId(),item.effectiveStartDate().toString(),item.effectiveEndDate()==null?"":item.effectiveEndDate().toString())).toList(); return attachment("position-assignments.xlsx", service.export("보직", List.of("보직코드","대상 사용자","소속조직","유효 시작일","유효 종료일"),rows)); }

    @GetMapping("/api/work-assignments") public ApiResponse<?> listWork(@RequestParam(required=false) String organizationId, @RequestParam(required=false) String userId, @RequestParam(required=false) String workArea, @RequestParam(required=false) LocalDate referenceDate, @RequestParam(defaultValue="0") Integer page, @RequestParam(defaultValue="20") Integer size) { return ApiResponse.success(service.findWorkAssignments(new AssignmentManagementMapper.WorkCriteria(organizationId,userId,workArea,referenceDate,page * size,size))); }
    @PostMapping("/api/work-assignments") public ApiResponse<Void> saveWork(@Valid @RequestBody WorkAssignmentRequest request) { service.saveWork(request); return ApiResponse.success(null); }
    @GetMapping("/api/work-assignments/export") public ResponseEntity<byte[]> exportWork(@RequestParam(required=false) String organizationId, @RequestParam(required=false) String userId, @RequestParam(required=false) String workArea, @RequestParam(required=false) LocalDate referenceDate) { var rows=service.findWorkAssignments(new AssignmentManagementMapper.WorkCriteria(organizationId,userId,workArea,referenceDate,null,null)).stream().map(item -> List.of(item.organizationId(),item.userId(),item.workArea(),item.dataScopeType(),item.processPermission())).toList(); return attachment("work-assignments.xlsx", service.export("업무담당자", List.of("업무조직","담당자","업무영역","데이터 범위","처리 권한"),rows)); }

    @GetMapping("/api/role-data-scopes") public ApiResponse<?> listScopes(@RequestParam(required=false) String roleCode, @RequestParam(required=false) String dataScopeType, @RequestParam(required=false) String organizationCode, @RequestParam(required=false) String workArea, @RequestParam(defaultValue="0") Integer page, @RequestParam(defaultValue="20") Integer size) { return ApiResponse.success(service.findRoleDataScopes(new AssignmentManagementMapper.RoleDataScopeCriteria(roleCode,dataScopeType,organizationCode,workArea,page * size,size))); }
    @PostMapping("/api/role-data-scopes") public ApiResponse<Void> saveScope(@Valid @RequestBody RoleDataScopeRequest request) { service.saveRoleDataScope(request); return ApiResponse.success(null); }
    @GetMapping("/api/role-data-scopes/export") public ResponseEntity<byte[]> exportScopes(@RequestParam(required=false) String roleCode, @RequestParam(required=false) String dataScopeType, @RequestParam(required=false) String organizationCode, @RequestParam(required=false) String workArea) { var rows=service.findRoleDataScopes(new AssignmentManagementMapper.RoleDataScopeCriteria(roleCode,dataScopeType,organizationCode,workArea,null,null)).stream().map(item -> List.of(item.roleCode(),item.dataScopeType(),item.organizationCode()==null?"":item.organizationCode(),item.workArea()==null?"":item.workArea())).toList(); return attachment("role-data-scopes.xlsx", service.export("데이터 범위", List.of("역할","데이터 범위","조직코드","업무영역"),rows)); }

    private ResponseEntity<byte[]> attachment(String filename, byte[] body) { return ResponseEntity.ok().contentType(XLSX).header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString()).body(body); }
}
