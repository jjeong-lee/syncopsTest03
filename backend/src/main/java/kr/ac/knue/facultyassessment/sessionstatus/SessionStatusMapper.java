package kr.ac.knue.facultyassessment.sessionstatus;

import java.time.OffsetDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SessionStatusMapper {

    @Select("select us.session_id as \"sessionId\", us.user_id as \"userId\", us.login_at as \"loginAt\", us.last_activity_at as \"lastActivityAt\", us.ip_address as \"ipAddress\", us.status as \"status\" from user_session us where us.status = 'ACTIVE' order by us.last_activity_at desc limit #{size} offset #{offset}")
    List<SessionStatusService.SessionStatus> findActiveSessions(@Param("size") int size, @Param("offset") int offset);

    @Select({"<script>",
        "select us.session_id as \"sessionId\", us.user_id as \"userId\", us.login_at as \"loginAt\", us.last_activity_at as \"lastActivityAt\", us.ip_address as \"ipAddress\", us.status as \"status\", us.termination_type as \"terminationType\", us.terminated_at as \"terminatedAt\", us.terminated_by as \"terminatedBy\", us.termination_reason as \"terminationReason\" from user_session us where us.status = 'TERMINATED' and us.termination_type in ('LOGOUT', 'IDLE_TIMEOUT', 'ABSOLUTE_TIMEOUT', 'ADMIN_TERMINATED')",
        "<if test='userId != null and userId != \"\"'> and us.user_id = #{userId}</if>",
        "<if test='startedAt != null'> and us.terminated_at &gt;= #{startedAt}</if>",
        "<if test='endedAt != null'> and us.terminated_at &lt;= #{endedAt}</if>",
        " order by us.terminated_at desc limit #{size} offset #{offset}",
        "</script>"})
    List<SessionStatusService.SessionHistory> findTerminationHistory(@Param("userId") String userId, @Param("startedAt") OffsetDateTime startedAt,
        @Param("endedAt") OffsetDateTime endedAt, @Param("size") int size, @Param("offset") int offset);

    @Select("select us.session_id as \"sessionId\", us.user_id as \"userId\", us.login_at as \"loginAt\", us.last_activity_at as \"lastActivityAt\", us.ip_address as \"ipAddress\", us.status as \"status\", us.termination_type as \"terminationType\", us.terminated_at as \"terminatedAt\", us.terminated_by as \"terminatedBy\", us.termination_reason as \"terminationReason\" from user_session us where us.session_id = #{sessionId} and us.status = 'TERMINATED'")
    SessionStatusService.SessionHistory findTerminationHistoryById(@Param("sessionId") String sessionId);

    @Select("select status from user_session where session_id = #{sessionId}")
    String findStatus(@Param("sessionId") String sessionId);

    @Update("update user_session set status = 'TERMINATED', termination_type = 'ADMIN_TERMINATED', terminated_at = current_timestamp, terminated_by = #{actorUserId}, termination_reason = #{reason}, updated_at = current_timestamp where session_id = #{sessionId} and status = 'ACTIVE'")
    int terminateActiveSession(@Param("sessionId") String sessionId, @Param("actorUserId") String actorUserId, @Param("reason") String reason);

    @Insert("insert into change_history (change_history_id, entity_name, entity_id, actor_user_id, changed_at, reason, status) values (#{historyId}, 'user_session', #{sessionId}, #{actorUserId}, current_timestamp, #{reason}, 'RECORDED')")
    void insertTerminationAudit(@Param("historyId") String historyId, @Param("sessionId") String sessionId, @Param("actorUserId") String actorUserId, @Param("reason") String reason);

    @Select("select change_history_id as \"changeHistoryId\", actor_user_id as \"actorUserId\", reason, changed_at as \"changedAt\" from change_history where entity_name = 'user_session' and entity_id = #{sessionId} order by changed_at desc limit 1")
    SessionStatusService.TerminationAudit findTerminationAudit(@Param("sessionId") String sessionId);
}
