package kr.ac.knue.fpe.common.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import kr.ac.knue.fpe.common.domain.AttachmentView;
import kr.ac.knue.fpe.common.domain.PageResult;
import kr.ac.knue.fpe.common.service.AttachmentService;
import kr.ac.knue.fpe.common.service.ValidationFailure;

@RestController
public class AttachmentController {
    private final AttachmentService service;
    public AttachmentController(AttachmentService service) { this.service = service; }

    @PostMapping("/api/admin/cmn/fr/052/053/054/attachments")
    public ResponseEntity<ApiResponse<AttachmentView>> upload(@RequestParam String businessKey, @RequestParam String reason, @RequestParam MultipartFile file) throws Exception {
        try { return ResponseEntity.status(201).body(ApiResponse.ok(service.upload(businessKey, reason, file))); }
        catch (ValidationFailure failure) { return ResponseEntity.badRequest().body(ApiResponse.fail(ApiError.validation(java.util.Map.of(failure.field(), failure.getMessage())))); }
    }

    @GetMapping("/api/admin/cmn/fr/052/053/054/attachments")
    public ApiResponse<PageResult<AttachmentView>> list() { return ApiResponse.ok(service.list()); }

    @GetMapping("/api/admin/cmn/fr/052/053/054/attachments/{attachmentId}/download")
    public ApiResponse<AttachmentView> download(@PathVariable String attachmentId) { return ApiResponse.ok(service.downloadMetadata(attachmentId)); }

    @DeleteMapping("/api/admin/cmn/fr/052/053/054/attachments/{attachmentId}")
    public ApiResponse<AttachmentView> delete(@PathVariable String attachmentId) { return ApiResponse.ok(service.delete(attachmentId)); }
}
