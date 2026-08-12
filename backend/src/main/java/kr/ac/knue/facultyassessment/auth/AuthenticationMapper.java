package kr.ac.knue.facultyassessment.auth;

import java.time.OffsetDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AuthenticationMapper {
    @Select("select user_id as \"userId\", password_hash as \"passwordHash\", password_salt as \"passwordSalt\", use_yn as \"useYn\" from user_account where user_id = #{userId}")
    AccountCredentials findAccount(@Param("userId") String userId);
    @Select("select role_code from user_role where user_id = #{userId} and status = 'ACTIVE' and effective_start_date <= current_date and (effective_end_date is null or effective_end_date >= current_date)")
    List<String> findActiveRoleCodes(@Param("userId") String userId);
    @Select("select authorized.menu_id as \"menuId\", authorized.menu_name as \"menuName\", authorized.parent_menu_id as \"parentMenuId\", authorized.route from (select distinct m.menu_id, m.menu_name, m.parent_menu_id, m.url as route, m.display_order from menu m join menu_permission mp on mp.menu_id = m.menu_id where mp.subject_type = 'ROLE' and mp.subject_id = #{roleCode} and mp.access_allowed = 'Y' and mp.status = 'ACTIVE' and m.use_yn = 'Y') authorized order by authorized.display_order")
    List<AuthenticationPort.AuthorizedMenu> findAuthorizedMenusForRole(@Param("roleCode") String roleCode);
    @Select("select authorized.menu_id as \"menuId\", authorized.menu_name as \"menuName\", authorized.parent_menu_id as \"parentMenuId\", authorized.route from (select distinct m.menu_id, m.menu_name, m.parent_menu_id, m.url as route, m.display_order from menu m join menu_permission mp on mp.menu_id = m.menu_id where mp.subject_type = 'USER' and mp.subject_id = #{userId} and mp.access_allowed = 'Y' and mp.status = 'ACTIVE' and m.use_yn = 'Y') authorized order by authorized.display_order")
    List<AuthenticationPort.AuthorizedMenu> findAuthorizedMenusForUser(@Param("userId") String userId);
    @Select("select authorized.menu_id as \"menuId\", authorized.menu_name as \"menuName\", authorized.parent_menu_id as \"parentMenuId\", authorized.route from (select distinct m.menu_id, m.menu_name, m.parent_menu_id, m.url as route, m.display_order from menu m join menu_permission mp on mp.menu_id = m.menu_id join organization_user_mapping oum on oum.organization_id = mp.subject_id where mp.subject_type = 'ORGANIZATION' and oum.user_id = #{userId} and oum.use_yn = 'Y' and mp.access_allowed = 'Y' and mp.status = 'ACTIVE' and m.use_yn = 'Y') authorized order by authorized.display_order")
    List<AuthenticationPort.AuthorizedMenu> findAuthorizedMenusForUserOrganizations(@Param("userId") String userId);
    @Insert("insert into user_session (session_id, user_id, ip_address, status) values (#{sessionId}, #{userId}, #{ipAddress}, 'ACTIVE')")
    void insertSession(@Param("sessionId") String sessionId, @Param("userId") String userId, @Param("ipAddress") String ipAddress);
    @Select("select us.session_id as \"sessionId\", ua.user_id as \"userId\" from user_session us join user_account ua on ua.user_id = us.user_id where us.session_id = #{sessionId} and us.status = 'ACTIVE' and ua.use_yn = 'Y'")
    ActiveSession findActiveSession(@Param("sessionId") String sessionId);
    @Insert("insert into session_end_history (session_id, user_id, login_at, ended_at, end_type, ip_address) select session_id, user_id, created_at, current_timestamp, #{endType}, ip_address from user_session where status = 'ACTIVE' and created_at <= current_timestamp - (#{ageMinutes} * interval '1 minute')")
    void recordExpiredSessions(@Param("ageMinutes") long ageMinutes, @Param("endType") String endType);
    @Update("update user_session set status = #{status}, ended_at = current_timestamp, updated_at = current_timestamp where status = 'ACTIVE' and created_at <= current_timestamp - (#{ageMinutes} * interval '1 minute')")
    void expireSessions(@Param("ageMinutes") long ageMinutes, @Param("status") String status);
    @Insert("insert into session_end_history (session_id, user_id, login_at, ended_at, end_type, ip_address) select session_id, user_id, created_at, current_timestamp, 'IDLE_EXPIRED', ip_address from user_session where status = 'ACTIVE' and last_activity_at <= current_timestamp - (#{ageMinutes} * interval '1 minute')")
    void recordIdleExpiredSessions(@Param("ageMinutes") long ageMinutes);
    @Update("update user_session set status = 'IDLE_EXPIRED', ended_at = current_timestamp, updated_at = current_timestamp where status = 'ACTIVE' and last_activity_at <= current_timestamp - (#{ageMinutes} * interval '1 minute')")
    void expireIdleSessions(@Param("ageMinutes") long ageMinutes);
    @Update("update user_session set last_activity_at = current_timestamp, updated_at = current_timestamp where session_id = #{sessionId} and status = 'ACTIVE'")
    void touchSession(@Param("sessionId") String sessionId);
    @Update("update user_session set status = case when #{endType} = 'ADMIN_FORCED' then 'FORCED_TERMINATED' else 'TERMINATED' end, ended_at = current_timestamp, updated_at = current_timestamp where session_id = #{sessionId} and status = 'ACTIVE'")
    int terminateSession(@Param("sessionId") String sessionId, @Param("endType") String endType);
    @Insert("insert into session_end_history (session_id, user_id, login_at, ended_at, end_type, actor_user_id, reason, ip_address) select session_id, user_id, created_at, ended_at, #{endType}, #{actorUserId}, #{reason}, ip_address from user_session where session_id = #{sessionId}")
    void insertSessionEndHistory(@Param("sessionId") String sessionId, @Param("endType") String endType, @Param("actorUserId") String actorUserId, @Param("reason") String reason);
    @Select("select session_id as \"sessionId\", user_id as \"userId\", created_at as \"loginAt\", last_activity_at as \"lastActivityAt\", ip_address as \"ipAddress\", status from user_session where status = 'ACTIVE' order by created_at desc")
    List<AuthenticationPort.SessionSummary> findActiveSessions();
    @Select("<script>select session_id as \"sessionId\", user_id as \"userId\", login_at as \"loginAt\", ended_at as \"endedAt\", end_type as \"endType\", actor_user_id as \"actorUserId\", reason, ip_address as \"ipAddress\" from session_end_history <where><if test='userId != null and userId != \"\"'>user_id = #{userId}</if><if test='startedAt != null'> and ended_at &gt;= #{startedAt}</if><if test='endedAt != null'> and ended_at &lt;= #{endedAt}</if></where> order by ended_at desc</script>")
    List<AuthenticationPort.SessionEndHistory> findSessionEndHistory(@Param("userId") String userId, @Param("startedAt") OffsetDateTime startedAt, @Param("endedAt") OffsetDateTime endedAt);
    record AccountCredentials(String userId, String passwordHash, String passwordSalt, String useYn) {}
    record ActiveSession(String sessionId, String userId) {}
}
