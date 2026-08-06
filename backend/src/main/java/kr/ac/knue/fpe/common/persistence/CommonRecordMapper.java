package kr.ac.knue.fpe.common.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import kr.ac.knue.fpe.common.domain.CommonRecord;

@Mapper
public interface CommonRecordMapper {
    @SelectProvider(type = CommonSqlProvider.class, method = "search")
    List<CommonRecord> search(@Param("featureCode") String featureCode, @Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);

    @Select("select count(*) from common_management_record where feature_code = #{featureCode}")
    long countByFeatureCode(@Param("featureCode") String featureCode);

    @Select("select record_id as \"recordId\", screen_id as \"screenId\", feature_code as \"featureCode\", entity_name as \"entityName\", title, status, payload_json as \"payloadJson\", created_at as \"createdAt\", updated_at as \"updatedAt\" from common_management_record where record_id = #{recordId}")
    CommonRecord findById(@Param("recordId") String recordId);

    @Insert("insert into common_management_record(record_id, screen_id, feature_code, entity_name, title, status, payload_json) values(#{recordId}, #{screenId}, #{featureCode}, #{entityName}, #{title}, #{status}, #{payloadJson})")
    void insert(CommonRecord record);

    @Update("update common_management_record set status = #{status}, payload_json = #{payloadJson}, updated_at = current_timestamp where record_id = #{recordId}")
    int update(@Param("recordId") String recordId, @Param("status") String status, @Param("payloadJson") String payloadJson);

    @Insert("insert into audit_log(actor_login_id, action_type, target_type, target_id, reason) values(#{actorLoginId}, #{actionType}, #{targetType}, #{targetId}, #{reason})")
    void insertAuditLog(@Param("actorLoginId") String actorLoginId, @Param("actionType") String actionType, @Param("targetType") String targetType, @Param("targetId") String targetId, @Param("reason") String reason);

    @Select("select count(*) from audit_log where target_id = #{targetId}")
    long countAuditLogByTargetId(@Param("targetId") String targetId);
}
