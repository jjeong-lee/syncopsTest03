package kr.ac.knue.fpe.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ManagedRecordService {
    private static final Map<String, String> AREA_TABLES = Map.ofEntries(
        Map.entry("organizations", "organizations"),
        Map.entry("positions", "position_assignments"),
        Map.entry("roles", "roles"),
        Map.entry("user-roles", "user_roles"),
        Map.entry("menu-permissions", "menu_permissions"),
        Map.entry("function-permissions", "function_permissions"),
        Map.entry("data-scope-permissions", "data_scope_permissions"),
        Map.entry("menus", "menus"),
        Map.entry("code-groups", "code_groups"),
        Map.entry("detail-codes", "detail_codes"),
        Map.entry("configurations", "system_configurations"),
        Map.entry("base-years", "base_years"),
        Map.entry("file-policies", "file_policies"),
        Map.entry("notices", "notices"),
        Map.entry("attachments", "attachment_files"),
        Map.entry("excel/templates", "excel_templates"),
        Map.entry("excel/uploads", "excel_upload_histories"),
        Map.entry("excel/downloads", "excel_templates"),
        Map.entry("privacy/policies", "privacy_policies"),
        Map.entry("batch-definitions", "batch_definitions"),
        Map.entry("batch-executions", "batch_execution_histories"),
        Map.entry("batch-results", "batch_results")
    );
    private static final Map<String, String> AREA_ALIASES = Map.ofEntries(
        Map.entry("admin/users", "users"),
        Map.entry("admin/organizations", "organizations"),
        Map.entry("admin/positions", "positions"),
        Map.entry("admin/roles", "roles"),
        Map.entry("admin/user-roles", "user-roles"),
        Map.entry("admin/menu-permissions", "menu-permissions"),
        Map.entry("admin/function-permissions", "function-permissions"),
        Map.entry("admin/data-scope-permissions", "data-scope-permissions"),
        Map.entry("admin/menus", "menus"),
        Map.entry("admin/code-groups", "code-groups"),
        Map.entry("admin/codes", "detail-codes"),
        Map.entry("admin/system-settings", "configurations"),
        Map.entry("admin/base-years", "base-years"),
        Map.entry("admin/file-policies", "file-policies"),
        Map.entry("admin/notices", "notices"),
        Map.entry("admin/attachments", "attachments"),
        Map.entry("admin/upload-templates", "excel/templates"),
        Map.entry("admin/excel-uploads", "excel/uploads"),
        Map.entry("admin/excel-downloads", "excel/downloads"),
        Map.entry("admin/personal-information", "privacy/policies"),
        Map.entry("admin/active-sessions", "sessions"),
        Map.entry("admin/audit-logs", "audit-logs"),
        Map.entry("admin/batch-definitions", "batch-definitions"),
        Map.entry("admin/batch-executions", "batch-executions"),
        Map.entry("admin/batch-results", "batch-results")
    );
    private final ManagedRecordMapper mapper;
    private final ObjectMapper objectMapper;
    public ManagedRecordService(ManagedRecordMapper mapper, ObjectMapper objectMapper) { this.mapper = mapper; this.objectMapper = objectMapper; }

    public List<Map<String, Object>> list(String area, String keyword, int page, int size) {
        int safeSize = List.of(20, 50, 100).contains(size) ? size : 20;
        int offset = Math.max(page, 0) * safeSize;
        List<Map<String, Object>> rows;
        if (area.equals("audit-logs")) {
            rows = mapper.listAudit(blankToNull(keyword), safeSize, offset);
        } else if (area.equals("sessions")) {
            rows = mapper.listSessions(blankToNull(keyword), safeSize, offset);
        } else if (area.equals("users")) {
            rows = mapper.listUsers(blankToNull(keyword), safeSize, offset);
        } else {
            rows = mapper.list(area, table(area), blankToNull(keyword), safeSize, offset);
        }
        rows.forEach(this::normalizePayload);
        return rows;
    }

    @Transactional
    public Map<String, Object> save(String area, ManagedRecord record, SessionUser actor) {
        area = canonicalArea(area);
        validateBusiness(area, record);
        if (area.equals("audit-logs")) {
            return appendAuditLog(record, actor);
        }
        if (area.equals("sessions")) {
            return terminateSession(record, actor);
        }
        Map<String, Object> before = area.equals("users") ? mapper.findUser(record.getId()) : mapper.find(area, table(area), record.getId());
        Map<String, Object> payload = new LinkedHashMap<>(record.getPayload());
        payload.putIfAbsent("area", area);
        String title = record.getTitle() == null || record.getTitle().isBlank() ? record.getId() : record.getTitle();
        String status = record.getStatus() == null || record.getStatus().isBlank() ? "ACTIVE" : record.getStatus();
        String useYn = record.getUseYn() == null || record.getUseYn().isBlank() ? "Y" : record.getUseYn();
        if (area.equals("users")) {
            String systemUseYn = Objects.toString(payload.getOrDefault("systemUseYn", useYn), useYn);
            mapper.upsertUser(record.getId(), title, Objects.toString(payload.getOrDefault("loginId", "")), systemUseYn, Objects.toString(payload.getOrDefault("primaryRoleCode", "")), status, systemUseYn, json(payload));
        } else {
            mapper.upsert(area, table(area), record.getId(), title, status, useYn, json(payload));
        }
        Map<String, Object> after = area.equals("users") ? mapper.findUser(record.getId()) : mapper.find(area, table(area), record.getId());
        mapper.audit("BUSINESS", area, record.getId(), actor.loginId(), "SAVE", Objects.toString(payload.getOrDefault("reason", "관리 화면 저장")), json(before), json(after));
        normalizePayload(after);
        return after;
    }

    private Map<String, Object> terminateSession(ManagedRecord record, SessionUser actor) {
        Map<String, Object> before = mapper.findSessionAny(record.getId());
        Map<String, Object> payload = new LinkedHashMap<>(record.getPayload());
        String reason = Objects.toString(payload.getOrDefault("reason", ""));
        if (before == null) {
            Map<String, Object> saved = new LinkedHashMap<>();
            saved.put("area", "sessions");
            saved.put("id", record.getId());
            saved.put("title", record.getTitle() == null ? record.getId() : record.getTitle());
            saved.put("status", "FORCED_END");
            saved.put("useYn", "Y");
            payload.putIfAbsent("endReason", reason.isBlank() ? "FORCED_END" : reason);
            saved.put("payload", payload);
            mapper.audit("BUSINESS", "sessions", record.getId(), actor.loginId(), "FORCED_END", reason.isBlank() ? "세션 강제종료" : reason, "{}", json(saved));
            return saved;
        }
        mapper.forceEndSession(record.getId(), reason.isBlank() ? "FORCED_END" : reason);
        Map<String, Object> after = mapper.findSessionAny(record.getId());
        mapper.audit("BUSINESS", "sessions", record.getId(), actor.loginId(), "FORCED_END", reason.isBlank() ? "세션 강제종료" : reason, json(before), json(after));
        normalizePayload(after);
        return after;
    }

    private Map<String, Object> appendAuditLog(ManagedRecord record, SessionUser actor) {
        Map<String, Object> payload = new LinkedHashMap<>(record.getPayload());
        payload.putIfAbsent("title", record.getTitle() == null ? record.getId() : record.getTitle());
        payload.putIfAbsent("area", "audit-logs");
        String reason = Objects.toString(payload.getOrDefault("reason", "감사 로그 수기 기록"));
        mapper.audit("BUSINESS", "audit-logs", record.getId(), actor.loginId(), "SAVE", reason, "{}", json(payload));
        Map<String, Object> saved = new LinkedHashMap<>();
        saved.put("area", "audit-logs");
        saved.put("id", record.getId());
        saved.put("title", record.getTitle() == null ? record.getId() : record.getTitle());
        saved.put("status", "RECORDED");
        saved.put("useYn", "Y");
        saved.put("payload", payload);
        return saved;
    }

    private String table(String area) {
        area = canonicalArea(area);
        String table = AREA_TABLES.get(area);
        if (table == null) {
            throw new AppException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "지원하지 않는 관리 영역입니다.", Map.of("area", area));
        }
        return table;
    }

    private String canonicalArea(String area) { return AREA_ALIASES.getOrDefault(area, area); }

    private void validateBusiness(String area, ManagedRecord record) {
        if (record.getId() == null || record.getId().isBlank()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "입력값을 확인해 주세요.", Map.of("id", "식별자는 필수입니다."));
        }
        Map<String, Object> p = record.getPayload();
        if (area.equals("attachments") && "Y".equals(Objects.toString(p.get("deleteYn"), "N"))) {
            if ("Y".equals(Objects.toString(p.get("finalizedRecordYn"), "N"))) {
                throw new AppException(HttpStatus.CONFLICT, "BUSINESS_RULE_VIOLATION", "평가확정 자료의 첨부파일은 논리삭제할 수 없습니다.", Map.of("finalizedRecordYn", "Y"));
            }
            if (Objects.toString(p.get("deleteReason"), "").isBlank()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "입력값을 확인해 주세요.", Map.of("deleteReason", "삭제 사유는 필수입니다."));
            }
        }
    }

    private String json(Object value) {
        try { return objectMapper.writeValueAsString(value == null ? Map.of() : value); }
        catch (JsonProcessingException e) { throw new IllegalStateException("JSON 직렬화 실패"); }
    }

    private void normalizePayload(Map<String, Object> record) {
        if (record == null) return;
        Object payload = record.get("payload");
        if (payload instanceof String payloadText && !payloadText.isBlank()) {
            try { record.put("payload", objectMapper.readValue(payloadText, Map.class)); }
            catch (JsonProcessingException e) { record.put("payload", Map.of("raw", payloadText)); }
        }
    }

    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
}
