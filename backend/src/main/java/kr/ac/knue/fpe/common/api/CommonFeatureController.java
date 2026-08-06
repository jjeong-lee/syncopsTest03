package kr.ac.knue.fpe.common.api;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import kr.ac.knue.fpe.common.domain.PageResult;
import kr.ac.knue.fpe.common.service.CommonFeatureService;
import kr.ac.knue.fpe.common.service.ValidationFailure;

@RestController
public class CommonFeatureController {
    private final CommonFeatureService service;

    public CommonFeatureController(CommonFeatureService service) {
        this.service = service;
    }

    @GetMapping("/api/admin/cmn/fr/001")
    public ApiResponse<PageResult<Map<String, Object>>> get001(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/001", keyword, filter, page, size);
    }

    @PatchMapping("/api/admin/cmn/fr/001/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> patch001UserId(@PathVariable String userId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/001/{userId}", body, userId, false);
    }

    @GetMapping("/api/admin/cmn/fr/002")
    public ApiResponse<PageResult<Map<String, Object>>> get002(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/002", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/002")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post002(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/002", body, null, true);
    }

    @PatchMapping("/api/admin/cmn/fr/002/{organizationCode}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> patch002OrganizationCode(@PathVariable String organizationCode, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/002/{organizationCode}", body, organizationCode, false);
    }

    @GetMapping("/api/admin/cmn/fr/003")
    public ApiResponse<PageResult<Map<String, Object>>> get003(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/003", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/003")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post003(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/003", body, null, true);
    }

    @PatchMapping("/api/admin/cmn/fr/003/{appointmentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> patch003AppointmentId(@PathVariable String appointmentId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/003/{appointmentId}", body, appointmentId, false);
    }

    @GetMapping("/api/admin/cmn/fr/005")
    public ApiResponse<PageResult<Map<String, Object>>> get005(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/005", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/005")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post005(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/005", body, null, true);
    }

    @PatchMapping("/api/admin/cmn/fr/005/{roleCode}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> patch005RoleCode(@PathVariable String roleCode, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/005/{roleCode}", body, roleCode, false);
    }

    @GetMapping("/api/admin/cmn/fr/006")
    public ApiResponse<PageResult<Map<String, Object>>> get006(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/006", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/006")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post006(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/006", body, null, true);
    }

    @DeleteMapping("/api/admin/cmn/fr/006/{userRoleId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> delete006UserRoleId(@PathVariable String userRoleId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/006/{userRoleId}", body, userRoleId, false);
    }

    @GetMapping("/api/admin/cmn/fr/007")
    public ApiResponse<PageResult<Map<String, Object>>> get007(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/007", keyword, filter, page, size);
    }

    @PutMapping("/api/admin/cmn/fr/007")
    public ResponseEntity<ApiResponse<Map<String, Object>>> put007(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/007", body, null, false);
    }

    @GetMapping("/api/admin/cmn/fr/008")
    public ApiResponse<PageResult<Map<String, Object>>> get008(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/008", keyword, filter, page, size);
    }

    @PutMapping("/api/admin/cmn/fr/008")
    public ResponseEntity<ApiResponse<Map<String, Object>>> put008(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/008", body, null, false);
    }

    @GetMapping("/api/admin/cmn/fr/009")
    public ApiResponse<PageResult<Map<String, Object>>> get009(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/009", keyword, filter, page, size);
    }

    @PutMapping("/api/admin/cmn/fr/009")
    public ResponseEntity<ApiResponse<Map<String, Object>>> put009(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/009", body, null, false);
    }

    @GetMapping("/api/admin/cmn/fr/013/014")
    public ApiResponse<PageResult<Map<String, Object>>> get013014(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/013/014", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/013/014")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post013014(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/013/014", body, null, true);
    }

    @PatchMapping("/api/admin/cmn/fr/013/014/{menuId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> patch013014MenuId(@PathVariable String menuId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/013/014/{menuId}", body, menuId, false);
    }

    @GetMapping("/api/admin/cmn/fr/016")
    public ApiResponse<PageResult<Map<String, Object>>> get016(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/016", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/016")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post016(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/016", body, null, true);
    }

    @PatchMapping("/api/admin/cmn/fr/016/{groupId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> patch016GroupId(@PathVariable String groupId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/016/{groupId}", body, groupId, false);
    }

    @GetMapping("/api/admin/cmn/fr/017")
    public ApiResponse<PageResult<Map<String, Object>>> get017(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/017", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/017")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post017(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/017", body, null, true);
    }

    @PatchMapping("/api/admin/cmn/fr/017/{detailCodeId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> patch017DetailCodeId(@PathVariable String detailCodeId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/017/{detailCodeId}", body, detailCodeId, false);
    }

    @GetMapping("/api/admin/cmn/fr/019")
    public ApiResponse<PageResult<Map<String, Object>>> get019(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/019", keyword, filter, page, size);
    }

    @PutMapping("/api/admin/cmn/fr/019")
    public ResponseEntity<ApiResponse<Map<String, Object>>> put019(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/019", body, null, false);
    }

    @GetMapping("/api/admin/cmn/fr/020")
    public ApiResponse<PageResult<Map<String, Object>>> get020(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/020", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/020")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post020(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/020", body, null, true);
    }

    @PatchMapping("/api/admin/cmn/fr/020/{baseYear}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> patch020BaseYear(@PathVariable String baseYear, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/020/{baseYear}", body, baseYear, false);
    }

    @GetMapping("/api/admin/cmn/fr/021")
    public ApiResponse<PageResult<Map<String, Object>>> get021(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/021", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/021")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post021(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/021", body, null, true);
    }

    @PatchMapping("/api/admin/cmn/fr/021/{filePolicyId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> patch021FilePolicyId(@PathVariable String filePolicyId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/021/{filePolicyId}", body, filePolicyId, false);
    }

    @GetMapping("/api/admin/cmn/fr/023")
    public ApiResponse<PageResult<Map<String, Object>>> get023(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/023", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/023")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post023(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/023", body, null, true);
    }

    @PatchMapping("/api/admin/cmn/fr/023/{noticeId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> patch023NoticeId(@PathVariable String noticeId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/023/{noticeId}", body, noticeId, false);
    }

    @GetMapping("/api/admin/cmn/fr/052/053/054")
    public ApiResponse<PageResult<Map<String, Object>>> get052053054(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/052/053/054", keyword, filter, page, size);
    }

    @DeleteMapping("/api/admin/cmn/fr/052/053/054/{fileId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> delete052053054FileId(@PathVariable String fileId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/052/053/054/{fileId}", body, fileId, false);
    }

    @PostMapping("/api/admin/cmn/fr/052/053/054/integrity-check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post052053054IntegrityCheck(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/052/053/054/integrity-check", body, null, true);
    }

    @GetMapping("/api/admin/cmn/fr/055")
    public ApiResponse<PageResult<Map<String, Object>>> get055(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/055", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/055")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post055(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/055", body, null, true);
    }

    @PatchMapping("/api/admin/cmn/fr/055/{templateId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> patch055TemplateId(@PathVariable String templateId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/055/{templateId}", body, templateId, false);
    }

    @GetMapping("/api/admin/cmn/fr/056")
    public ApiResponse<PageResult<Map<String, Object>>> get056(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/056", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/056/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post056Upload(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/056/upload", body, null, true);
    }

    @GetMapping("/api/admin/cmn/fr/056/history")
    public ApiResponse<PageResult<Map<String, Object>>> get056History(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/056/history", keyword, filter, page, size);
    }

    @GetMapping("/api/admin/cmn/fr/056/errors/download")
    public ApiResponse<PageResult<Map<String, Object>>> get056ErrorsDownload(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/056/errors/download", keyword, filter, page, size);
    }

    @GetMapping("/api/admin/cmn/fr/059")
    public ApiResponse<PageResult<Map<String, Object>>> get059(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/059", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/059/download")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post059Download(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/059/download", body, null, true);
    }

    @GetMapping("/api/admin/cmn/fr/071/072/073")
    public ApiResponse<PageResult<Map<String, Object>>> get071072073(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/071/072/073", keyword, filter, page, size);
    }

    @PutMapping("/api/admin/cmn/fr/071/072/073")
    public ResponseEntity<ApiResponse<Map<String, Object>>> put071072073(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/071/072/073", body, null, false);
    }

    @GetMapping("/api/admin/cmn/fr/074")
    public ApiResponse<PageResult<Map<String, Object>>> get074(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/074", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/074/{sessionId}/terminate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post074SessionIdTerminate(@PathVariable String sessionId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/074/{sessionId}/terminate", body, sessionId, true);
    }

    @GetMapping("/api/admin/cmn/fr/074/termination-history")
    public ApiResponse<PageResult<Map<String, Object>>> get074TerminationHistory(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/074/termination-history", keyword, filter, page, size);
    }

    @GetMapping("/api/admin/cmn/fr/076/077/078")
    public ApiResponse<PageResult<Map<String, Object>>> get076077078(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/076/077/078", keyword, filter, page, size);
    }

    @GetMapping("/api/admin/cmn/fr/079")
    public ApiResponse<PageResult<Map<String, Object>>> get079(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/079", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/079")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post079(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/079", body, null, true);
    }

    @PatchMapping("/api/admin/cmn/fr/079/{batchId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> patch079BatchId(@PathVariable String batchId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/079/{batchId}", body, batchId, false);
    }

    @GetMapping("/api/admin/cmn/fr/080")
    public ApiResponse<PageResult<Map<String, Object>>> get080(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/080", keyword, filter, page, size);
    }

    @PostMapping("/api/admin/cmn/fr/080/{batchId}/run")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post080BatchIdRun(@PathVariable String batchId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/080/{batchId}/run", body, batchId, true);
    }

    @PostMapping("/api/admin/cmn/fr/080/{batchId}/stop")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post080BatchIdStop(@PathVariable String batchId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/080/{batchId}/stop", body, batchId, true);
    }

    @PostMapping("/api/admin/cmn/fr/080/{batchId}/rerun")
    public ResponseEntity<ApiResponse<Map<String, Object>>> post080BatchIdRerun(@PathVariable String batchId, @RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/080/{batchId}/rerun", body, batchId, true);
    }

    @GetMapping("/api/admin/cmn/fr/081")
    public ApiResponse<PageResult<Map<String, Object>>> get081(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/081", keyword, filter, page, size);
    }

    @GetMapping("/api/admin/cmn/fr/055/{templateId}/download")
    public ApiResponse<PageResult<Map<String, Object>>> get055TemplateIdDownload(@PathVariable String templateId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/055/{templateId}/download", keyword, filter, page, size);
    }

    @GetMapping("/api/admin/cmn/fr/076/077/078/business")
    public ApiResponse<PageResult<Map<String, Object>>> get076077078Business(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/076/077/078/business", keyword, filter, page, size);
    }

    @GetMapping("/api/admin/cmn/fr/076/077/078/sensitive")
    public ApiResponse<PageResult<Map<String, Object>>> get076077078Sensitive(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/076/077/078/sensitive", keyword, filter, page, size);
    }

    @GetMapping("/api/admin/cmn/fr/076/077/078/authority")
    public ApiResponse<PageResult<Map<String, Object>>> get076077078Authority(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/076/077/078/authority", keyword, filter, page, size);
    }

    @GetMapping("/api/admin/cmn/fr/071/072/073/access-permissions")
    public ApiResponse<PageResult<Map<String, Object>>> get071072073AccessPermissions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/071/072/073/access-permissions", keyword, filter, page, size);
    }

    @PutMapping("/api/admin/cmn/fr/071/072/073/access-permissions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> put071072073AccessPermissions(@RequestBody(required = false) Map<String, Object> body) {
        return write("/api/admin/cmn/fr/071/072/073/access-permissions", body, null, false);
    }

    @GetMapping("/api/admin/cmn/fr/071/072/073/access-history")
    public ApiResponse<PageResult<Map<String, Object>>> get071072073AccessHistory(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter) {
        return read("/api/admin/cmn/fr/071/072/073/access-history", keyword, filter, page, size);
    }

    private ApiResponse<PageResult<Map<String, Object>>> read(String requestPath, String keyword, String filter, int page, int size) {
        return ApiResponse.ok(service.search(requestPath, keyword != null ? keyword : filter, page, size));
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> write(String requestPath, Map<String, Object> body, String recordId, boolean created) {
        try {
            Map<String, Object> normalized = normalizeBody(requestPath, body);
            var saved = service.save(requestPath, recordId, normalized);
            return ResponseEntity.status(created ? 201 : 200).body(ApiResponse.ok(saved));
        } catch (ValidationFailure failure) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ApiError.validation(Map.of(failure.field(), failure.getMessage()))));
        }
    }

    private Map<String, Object> normalizeBody(String requestPath, Map<String, Object> body) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        if (body != null) normalized.putAll(body);
        if (requestPath.endsWith("/run") || requestPath.endsWith("/rerun")) {
            normalized.putIfAbsent("status", "RUNNING");
            normalized.putIfAbsent("stateTransition", "READY->RUNNING");
        } else if (requestPath.endsWith("/stop")) {
            normalized.putIfAbsent("status", "STOPPED");
            normalized.putIfAbsent("stateTransition", "RUNNING->STOPPED");
        } else if (requestPath.endsWith("/terminate")) {
            normalized.putIfAbsent("status", "FORCED_TERMINATED");
            normalized.putIfAbsent("stateTransition", "ACTIVE->FORCED_TERMINATED");
        } else if (requestPath.contains("/upload")) {
            normalized.putIfAbsent("status", "VALIDATED");
            normalized.putIfAbsent("stateTransition", "SAVED->VALIDATED");
        } else if (requestPath.contains("/integrity-check")) {
            normalized.putIfAbsent("status", "COMPLETED");
        } else if (requestPath.contains("/download")) {
            normalized.putIfAbsent("status", "COMPLETED");
        } else if (requestPath.contains("/access-permissions")) {
            normalized.putIfAbsent("status", "ACTIVE");
            normalized.putIfAbsent("sideEffect", "privacy_access_permission");
        } else if (requestPath.startsWith("/api/admin/cmn/fr/052/053/054/")) {
            normalized.putIfAbsent("status", "LOGICALLY_DELETED");
            normalized.putIfAbsent("stateTransition", "ACTIVE->LOGICALLY_DELETED");
        } else if (requestPath.startsWith("/api/admin/cmn/fr/006/")) {
            normalized.putIfAbsent("status", "REVOKED");
            normalized.putIfAbsent("stateTransition", "ACTIVE->REVOKED");
        } else {
            normalized.putIfAbsent("status", "ACTIVE");
        }
        return normalized;
    }
}
