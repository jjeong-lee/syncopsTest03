package kr.ac.knue.fpe.common.persistence;

import java.util.Map;

public class CommonSqlProvider {
    public String search(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("select record_id as \"recordId\", screen_id as \"screenId\", feature_code as \"featureCode\", entity_name as \"entityName\", title, status, payload_json as \"payloadJson\", created_at as \"createdAt\", updated_at as \"updatedAt\" from common_management_record where feature_code = #{featureCode}");
        if (params.get("keyword") != null && !params.get("keyword").toString().isBlank()) {
            sql.append(" and (lower(title) like lower(concat('%', #{keyword}, '%')) or lower(payload_json) like lower(concat('%', #{keyword}, '%'))) ");
        }
        sql.append(" order by updated_at desc limit #{limit} offset #{offset}");
        return sql.toString();
    }
}
