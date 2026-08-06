package kr.ac.knue.fpe.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kr.ac.knue.fpe.common.domain.CommonRecord;
import kr.ac.knue.fpe.common.domain.FeatureCatalog;
import kr.ac.knue.fpe.common.domain.PageResult;
import kr.ac.knue.fpe.common.persistence.CommonRecordMapper;

@Service
public class CommonFeatureService {
    private final CommonRecordMapper mapper;
    private final ObjectMapper objectMapper;
    public CommonFeatureService(CommonRecordMapper mapper, ObjectMapper objectMapper) { this.mapper = mapper; this.objectMapper = objectMapper; }

    public PageResult<Map<String, Object>> search(String requestPath, String keyword, int page, int size) {
        FeatureCatalog.Screen screen = FeatureCatalog.byApiPath(requestPath);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        var rows = mapper.search(featureCode(screen.apiPath()), keyword, safeSize, safePage * safeSize).stream().map(this::toMap).toList();
        return new PageResult<>(rows, new PageResult.PageInfo(safePage, safeSize, mapper.countByFeatureCode(featureCode(screen.apiPath()))));
    }

    @Transactional
    public Map<String, Object> save(String requestPath, String recordId, Map<String, Object> body) {
        if (body == null) body = Map.of();
        Object reason = body.get("reason");
        if (reason == null || reason.toString().isBlank()) throw new ValidationFailure("reason", "변경 사유는 필수입니다.");
        FeatureCatalog.Screen screen = FeatureCatalog.byApiPath(requestPath);
        Map<String, Object> payload = new LinkedHashMap<>(body);
        payload.put("auditReason", reason.toString());
        payload.put("updatedBy", "admin");
        String id = recordId == null || recordId.isBlank() ? screen.primaryEntity().toUpperCase() + "-" + System.currentTimeMillis() : recordId;
        String status = payload.getOrDefault("status", "ACTIVE").toString();
        String json = writeJson(payload);
        CommonRecord existing = mapper.findById(id);
        if (existing == null) {
            mapper.insert(new CommonRecord(id, screen.screenId(), featureCode(screen.apiPath()), screen.primaryEntity(), screen.menuPath().substring(screen.menuPath().lastIndexOf('>') + 1).trim(), status, json, null, null));
        } else {
            mapper.update(id, status, json);
        }
        mapper.insertAuditLog("admin", existing == null ? "CREATE" : auditAction(status), screen.primaryEntity(), id, reason.toString());
        return toMap(mapper.findById(id));
    }

    public long countAuditLogByTargetId(String targetId) {
        return mapper.countAuditLogByTargetId(targetId);
    }

    private String featureCode(String apiPath) { return apiPath.substring("/api/admin/cmn/fr/".length()).replace('/', '-'); }
    private String writeJson(Map<String, Object> payload) { try { return objectMapper.writeValueAsString(payload); } catch (Exception e) { return "{}"; } }
    private String auditAction(String status) {
        return switch (status) {
            case "REVOKED", "LOGICALLY_DELETED" -> "DELETE";
            case "ACTIVE", "RUNNING", "STOPPED", "FORCED_TERMINATED", "VALIDATED", "COMPLETED" -> "UPDATE";
            default -> "UPDATE";
        };
    }
    private Map<String, Object> toMap(CommonRecord record) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("recordId", record.recordId()); map.put("screenId", record.screenId()); map.put("featureCode", record.featureCode()); map.put("entityName", record.entityName()); map.put("title", record.title()); map.put("status", record.status()); map.put("createdAt", record.createdAt()); map.put("updatedAt", record.updatedAt());
        try { if (record.payloadJson() != null) map.putAll(objectMapper.readValue(record.payloadJson(), Map.class)); } catch (Exception ignored) { }
        return map;
    }
}
