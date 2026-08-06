package kr.ac.knue.fpe.common.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import kr.ac.knue.fpe.common.domain.AttachmentView;

@Mapper
public interface AttachmentMapper {
    @Insert("insert into attachment_metadata(attachment_id, business_key, original_file_name, size_bytes, content_type, storage_key, malware_scan_status, delete_yn) values(#{attachmentId}, #{businessKey}, #{originalFileName}, #{sizeBytes}, #{contentType}, #{storageKey}, #{malwareScanStatus}, #{deleteYn})")
    void insert(AttachmentView attachment);

    @Select("select attachment_id as \"attachmentId\", business_key as \"businessKey\", original_file_name as \"originalFileName\", size_bytes as \"sizeBytes\", content_type as \"contentType\", storage_key as \"storageKey\", malware_scan_status as \"malwareScanStatus\", delete_yn as \"deleteYn\", created_at as \"createdAt\" from attachment_metadata where delete_yn = 'N' order by created_at desc")
    List<AttachmentView> list();

    @Select("select attachment_id as \"attachmentId\", business_key as \"businessKey\", original_file_name as \"originalFileName\", size_bytes as \"sizeBytes\", content_type as \"contentType\", storage_key as \"storageKey\", malware_scan_status as \"malwareScanStatus\", delete_yn as \"deleteYn\", created_at as \"createdAt\" from attachment_metadata where attachment_id = #{attachmentId}")
    AttachmentView find(@Param("attachmentId") String attachmentId);

    @Update("update attachment_metadata set delete_yn = 'Y', updated_at = current_timestamp where attachment_id = #{attachmentId} and delete_yn = 'N'")
    int logicalDelete(@Param("attachmentId") String attachmentId);
}
