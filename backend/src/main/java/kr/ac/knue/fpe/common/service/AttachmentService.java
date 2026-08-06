package kr.ac.knue.fpe.common.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import kr.ac.knue.fpe.common.domain.AttachmentView;
import kr.ac.knue.fpe.common.domain.PageResult;
import kr.ac.knue.fpe.common.persistence.AttachmentMapper;

@Service
public class AttachmentService {
    private final AttachmentMapper mapper;
    private final Path storageDir;
    public AttachmentService(AttachmentMapper mapper, @Value("${app.attachment-storage-dir}") String storageDir) { this.mapper = mapper; this.storageDir = Path.of(storageDir); }

    public AttachmentView upload(String businessKey, String reason, MultipartFile file) throws Exception {
        if (reason == null || reason.isBlank()) throw new ValidationFailure("reason", "변경 사유는 필수입니다.");
        if (file == null || file.isEmpty()) throw new ValidationFailure("file", "파일은 필수입니다.");
        Files.createDirectories(storageDir);
        String id = "ATT-" + UUID.randomUUID();
        String storageKey = id + "-" + file.getOriginalFilename();
        file.transferTo(storageDir.resolve(storageKey));
        AttachmentView view = new AttachmentView(id, businessKey, file.getOriginalFilename(), file.getSize(), file.getContentType(), storageKey, "PASSED", "N", null);
        mapper.insert(view);
        return mapper.find(id);
    }

    public PageResult<AttachmentView> list() { var rows = mapper.list(); return new PageResult<>(rows, new PageResult.PageInfo(0, rows.size(), rows.size())); }
    public AttachmentView downloadMetadata(String attachmentId) { AttachmentView view = mapper.find(attachmentId); if (view == null) throw new IllegalArgumentException("첨부파일을 찾을 수 없습니다."); return view; }
    public AttachmentView delete(String attachmentId) { mapper.logicalDelete(attachmentId); return mapper.find(attachmentId); }
}
