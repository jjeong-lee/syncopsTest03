package kr.ac.knue.facultyassessment.assignments;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;
import kr.ac.knue.facultyassessment.common.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class AssignmentManagementService {
    private static final List<String> DATA_SCOPE_TYPES = List.of("본인", "소속학과", "단과대학", "담당업무", "전체");
    private final AssignmentManagementMapper mapper;
    private final EffectiveDateValidator effectiveDateValidator;

    public AssignmentManagementService(AssignmentManagementMapper mapper, EffectiveDateValidator effectiveDateValidator) {
        this.mapper = mapper;
        this.effectiveDateValidator = effectiveDateValidator;
    }

    public List<AssignmentManagementMapper.PositionAssignment> findPositions(AssignmentManagementMapper.PositionCriteria criteria) { return mapper.findPositionAssignments(criteria); }
    public List<AssignmentManagementMapper.WorkAssignment> findWorkAssignments(AssignmentManagementMapper.WorkCriteria criteria) { return mapper.findWorkAssignments(criteria); }
    public List<AssignmentManagementMapper.RoleDataScope> findRoleDataScopes(AssignmentManagementMapper.RoleDataScopeCriteria criteria) { return mapper.findRoleDataScopes(criteria); }

    @Transactional
    public void savePosition(PositionAssignmentRequest request) {
        effectiveDateValidator.validate(request.effectiveStartDate(), request.effectiveEndDate());
        require(mapper.userExists(request.userId()), "USER_NOT_FOUND", "대상 사용자를 찾을 수 없습니다.", "userId");
        require(mapper.organizationExists(request.organizationId()), "ORGANIZATION_NOT_FOUND", "소속조직을 찾을 수 없습니다.", "organizationId");
        require(!mapper.hasPositionOverlap(request), "OVERLAPPING_EFFECTIVE_PERIOD", "동일 보직·사용자·조직의 유효기간이 겹칩니다.", "effectiveStartDate");
        if (request.positionAssignmentId() == null || request.positionAssignmentId().isBlank()) mapper.insertPosition("POSITION-" + UUID.randomUUID(), request); else mapper.updatePosition(request);
    }

    @Transactional
    public void saveWork(WorkAssignmentRequest request) {
        effectiveDateValidator.validate(request.effectiveStartDate(), request.effectiveEndDate());
        require(mapper.userExists(request.userId()), "USER_NOT_FOUND", "담당자를 찾을 수 없습니다.", "userId");
        require(mapper.organizationExists(request.organizationId()), "ORGANIZATION_NOT_FOUND", "업무조직을 찾을 수 없습니다.", "organizationId");
        validateScope(request.dataScopeType());
        require("Y".equals(request.processPermission()) || "N".equals(request.processPermission()), "INVALID_PROCESS_PERMISSION", "처리 권한은 Y 또는 N이어야 합니다.", "processPermission");
        require(!mapper.hasWorkOverlap(request), "OVERLAPPING_EFFECTIVE_PERIOD", "동일 업무조직·담당자·업무영역의 지정기간이 겹칩니다.", "effectiveStartDate");
        if (request.workAssignmentId() == null || request.workAssignmentId().isBlank()) mapper.insertWork("WORK-" + UUID.randomUUID(), request); else mapper.updateWork(request);
    }

    @Transactional
    public void saveRoleDataScope(RoleDataScopeRequest request) {
        require(mapper.roleExists(request.roleCode()), "ROLE_NOT_FOUND", "역할을 찾을 수 없습니다.", "roleCode");
        validateScope(request.dataScopeType());
        if (request.organizationCode() != null && !request.organizationCode().isBlank()) require(mapper.organizationCodeExists(request.organizationCode()), "ORGANIZATION_NOT_FOUND", "조직코드를 찾을 수 없습니다.", "organizationCode");
        if (request.roleDataScopeId() == null || request.roleDataScopeId().isBlank()) mapper.insertRoleDataScope("ROLE-SCOPE-" + UUID.randomUUID(), request); else mapper.updateRoleDataScope(request);
    }

    public byte[] export(String title, List<String> headers, List<List<String>> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet(title);
            var header = sheet.createRow(0);
            for (int index = 0; index < headers.size(); index++) header.createCell(index).setCellValue(headers.get(index));
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                var row = sheet.createRow(rowIndex + 1);
                for (int columnIndex = 0; columnIndex < rows.get(rowIndex).size(); columnIndex++) row.createCell(columnIndex).setCellValue(rows.get(rowIndex).get(columnIndex));
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (Exception exception) { throw new IllegalStateException("Excel 파일을 생성할 수 없습니다.", exception); }
    }

    private void validateScope(String dataScopeType) { require(DATA_SCOPE_TYPES.contains(dataScopeType), "INVALID_DATA_SCOPE_TYPE", "데이터 범위 유형이 올바르지 않습니다.", "dataScopeType"); }
    private void require(boolean condition, String code, String message, String field) { if (!condition) throw new ApiException(HttpStatus.BAD_REQUEST, code, message, field); }
}
