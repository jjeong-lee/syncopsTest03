package kr.ac.knue.fpe.common;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface ManagedRecordMapper {
    @SelectProvider(type = SqlProvider.class, method = "list")
    List<Map<String, Object>> list(@Param("area") String area, @Param("table") String table, @Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);

    @SelectProvider(type = SqlProvider.class, method = "find")
    Map<String, Object> find(@Param("area") String area, @Param("table") String table, @Param("id") String id);

    @InsertProvider(type = SqlProvider.class, method = "upsert")
    void upsert(@Param("area") String area, @Param("table") String table, @Param("id") String id, @Param("title") String title, @Param("status") String status, @Param("useYn") String useYn, @Param("payloadJson") String payloadJson);

    @SelectProvider(type = SqlProvider.class, method = "listUsers")
    List<Map<String, Object>> listUsers(@Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);

    @SelectProvider(type = SqlProvider.class, method = "findUser")
    Map<String, Object> findUser(@Param("id") String id);

    @Insert("insert into user_accounts(account_id, employee_no, name, login_id, system_use_yn, primary_role_code, status, use_yn, payload) values(#{id}, #{id}, #{title}, nullif(#{loginId}, ''), #{systemUseYn}, nullif(#{primaryRoleCode}, ''), #{status}, #{useYn}, cast(#{payloadJson} as jsonb)) on conflict(account_id) do update set name = excluded.name, system_use_yn = excluded.system_use_yn, primary_role_code = excluded.primary_role_code, status = excluded.status, use_yn = excluded.use_yn, payload = excluded.payload, updated_at = now()")
    void upsertUser(@Param("id") String id, @Param("title") String title, @Param("loginId") String loginId, @Param("systemUseYn") String systemUseYn, @Param("primaryRoleCode") String primaryRoleCode, @Param("status") String status, @Param("useYn") String useYn, @Param("payloadJson") String payloadJson);

    @Insert("insert into audit_logs(audit_type, target_area, target_id, actor_login_id, action, reason, before_payload, after_payload) values(#{auditType}, #{targetArea}, #{targetId}, #{actorLoginId}, #{action}, #{reason}, cast(#{beforePayload} as jsonb), cast(#{afterPayload} as jsonb))")
    void audit(@Param("auditType") String auditType, @Param("targetArea") String targetArea, @Param("targetId") String targetId, @Param("actorLoginId") String actorLoginId, @Param("action") String action, @Param("reason") String reason, @Param("beforePayload") String beforePayload, @Param("afterPayload") String afterPayload);

    @SelectProvider(type = SqlProvider.class, method = "listAudit")
    List<Map<String, Object>> listAudit(@Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);

    @SelectProvider(type = SqlProvider.class, method = "listSessions")
    List<Map<String, Object>> listSessions(@Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);

    @Insert("insert into app_sessions(session_id, login_id, user_name, role_code, ip_address) values(#{sessionId}, #{loginId}, #{userName}, #{roleCode}, #{ipAddress})")
    void createSession(@Param("sessionId") String sessionId, @Param("loginId") String loginId, @Param("userName") String userName, @Param("roleCode") String roleCode, @Param("ipAddress") String ipAddress);

    @Select("select session_id as \"sessionId\", login_id as \"loginId\", user_name as \"userName\", role_code as \"roleCode\", session_status as \"sessionStatus\" from app_sessions where session_id = #{sessionId} and session_status = 'ACTIVE'")
    Map<String, Object> session(@Param("sessionId") String sessionId);

    @Select("select 'sessions' as area, session_id as \"id\", user_name as title, session_status as status, 'Y' as \"useYn\", created_at as \"createdAt\", updated_at as \"updatedAt\", jsonb_build_object('loginId', login_id, 'roleCode', role_code, 'ipAddress', ip_address, 'endedAt', ended_at, 'endReason', end_reason) as payload from app_sessions where session_id = #{sessionId}")
    Map<String, Object> findSessionAny(@Param("sessionId") String sessionId);

    @Update("update app_sessions set session_status='LOGGED_OUT', ended_at=now(), end_reason=#{reason}, updated_at=now() where session_id=#{sessionId}")
    void endSession(@Param("sessionId") String sessionId, @Param("reason") String reason);

    @Update("update app_sessions set session_status='FORCED_END', ended_at=now(), end_reason=#{reason}, updated_at=now() where session_id=#{sessionId}")
    void forceEndSession(@Param("sessionId") String sessionId, @Param("reason") String reason);

    class SqlProvider {
        public String list(Map<String, Object> params) {
            StringBuilder sql = new StringBuilder("select #{area} as area, id as \"id\", name as title, status, use_yn as \"useYn\", created_at as \"createdAt\", updated_at as \"updatedAt\", payload from ${table} where 1=1");
            if (params.get("keyword") != null && !params.get("keyword").toString().isBlank()) {
                sql.append(" and (id ilike '%' || #{keyword} || '%' or name ilike '%' || #{keyword} || '%' or payload::text ilike '%' || #{keyword} || '%')");
            }
            sql.append(" order by updated_at desc, id limit #{limit} offset #{offset}");
            return sql.toString();
        }

        public String find(Map<String, Object> params) {
            return "select #{area} as area, id as \"id\", name as title, status, use_yn as \"useYn\", created_at as \"createdAt\", updated_at as \"updatedAt\", payload from ${table} where id = #{id}";
        }

        public String upsert(Map<String, Object> params) {
            return "insert into ${table}(id, name, status, use_yn, payload) values(#{id}, #{title}, #{status}, #{useYn}, cast(#{payloadJson} as jsonb)) on conflict(id) do update set name = excluded.name, status = excluded.status, use_yn = excluded.use_yn, payload = excluded.payload, updated_at = now()";
        }

        public String listUsers(Map<String, Object> params) {
            StringBuilder sql = new StringBuilder("select 'users' as area, ua.account_id as \"id\", coalesce(k.name, ua.name) as title, ua.status, ua.use_yn as \"useYn\", ua.created_at as \"createdAt\", ua.updated_at as \"updatedAt\", ua.payload || jsonb_build_object('employeeNo', k.employee_no, 'organizationCode', k.organization_code, 'rankName', k.position_grade, 'employmentStatus', k.employment_status, 'retirementDate', k.retirement_date, 'lastSyncedAt', k.last_sync_at, 'systemUseYn', ua.system_use_yn, 'primaryRoleCode', ua.primary_role_code) as payload from user_accounts ua left join k_o_r_u_s_mock_snapshot k on k.employee_no = ua.employee_no where 1=1");
            if (params.get("keyword") != null && !params.get("keyword").toString().isBlank()) {
                sql.append(" and (ua.account_id ilike '%' || #{keyword} || '%' or ua.name ilike '%' || #{keyword} || '%' or k.name ilike '%' || #{keyword} || '%' or k.organization_code ilike '%' || #{keyword} || '%' or ua.primary_role_code ilike '%' || #{keyword} || '%')");
            }
            sql.append(" order by ua.updated_at desc, ua.account_id limit #{limit} offset #{offset}");
            return sql.toString();
        }

        public String findUser(Map<String, Object> params) {
            return "select 'users' as area, ua.account_id as \"id\", coalesce(k.name, ua.name) as title, ua.status, ua.use_yn as \"useYn\", ua.created_at as \"createdAt\", ua.updated_at as \"updatedAt\", ua.payload || jsonb_build_object('employeeNo', k.employee_no, 'organizationCode', k.organization_code, 'rankName', k.position_grade, 'employmentStatus', k.employment_status, 'retirementDate', k.retirement_date, 'lastSyncedAt', k.last_sync_at, 'systemUseYn', ua.system_use_yn, 'primaryRoleCode', ua.primary_role_code) as payload from user_accounts ua left join k_o_r_u_s_mock_snapshot k on k.employee_no = ua.employee_no where ua.account_id = #{id}";
        }

        public String listAudit(Map<String, Object> params) {
            StringBuilder sql = new StringBuilder("select 'audit-logs' as area, audit_id::text as \"id\", target_area || ' ' || action as title, status, 'Y' as \"useYn\", created_at as \"createdAt\", updated_at as \"updatedAt\", jsonb_build_object('action', action, 'targetArea', target_area, 'targetId', target_id, 'actorLoginId', actor_login_id, 'reason', reason, 'before', before_payload, 'after', after_payload) as payload from audit_logs where 1=1");
            if (params.get("keyword") != null && !params.get("keyword").toString().isBlank()) {
                sql.append(" and (target_area ilike '%' || #{keyword} || '%' or target_id ilike '%' || #{keyword} || '%' or action ilike '%' || #{keyword} || '%')");
            }
            sql.append(" order by created_at desc limit #{limit} offset #{offset}");
            return sql.toString();
        }

        public String listSessions(Map<String, Object> params) {
            StringBuilder sql = new StringBuilder("select 'sessions' as area, session_id as \"id\", user_name as title, session_status as status, 'Y' as \"useYn\", created_at as \"createdAt\", updated_at as \"updatedAt\", jsonb_build_object('loginId', login_id, 'roleCode', role_code, 'ipAddress', ip_address, 'endedAt', ended_at, 'endReason', end_reason) as payload from app_sessions where 1=1");
            if (params.get("keyword") != null && !params.get("keyword").toString().isBlank()) {
                sql.append(" and (login_id ilike '%' || #{keyword} || '%' or user_name ilike '%' || #{keyword} || '%' or session_status ilike '%' || #{keyword} || '%')");
            }
            sql.append(" order by created_at desc limit #{limit} offset #{offset}");
            return sql.toString();
        }
    }
}
