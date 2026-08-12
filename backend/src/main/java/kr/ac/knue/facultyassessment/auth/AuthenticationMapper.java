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

    @Insert("insert into user_session (session_id, user_id, status, login_at, last_activity_at, ip_address) values (#{sessionId}, #{userId}, 'ACTIVE', current_timestamp, current_timestamp, #{ipAddress})")
    void insertSession(@Param("sessionId") String sessionId, @Param("userId") String userId, @Param("ipAddress") String ipAddress);

    @Select("select us.session_id as \"sessionId\", ua.user_id as \"userId\" from user_session us join user_account ua on ua.user_id = us.user_id where us.session_id = #{sessionId} and us.status = 'ACTIVE' and ua.use_yn = 'Y'")
    ActiveSession findActiveSession(@Param("sessionId") String sessionId);

    @Update("update user_session set status = 'TERMINATED', termination_type = 'LOGOUT', terminated_at = current_timestamp, updated_at = current_timestamp where session_id = #{sessionId} and status = 'ACTIVE'")
    void terminateSession(@Param("sessionId") String sessionId);

    record AccountCredentials(String userId, String passwordHash, String passwordSalt, String useYn) {
    }

    record ActiveSession(String sessionId, String userId) {
    }
}
