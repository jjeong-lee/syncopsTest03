package kr.ac.knue.facultyeval.auth;

import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;

@Mapper
public interface AuthenticationPort {
  @Select("select user_id as \"userId\", login_id as \"loginId\", display_name as \"displayName\", password_hash as \"passwordHash\", system_enabled as \"systemEnabled\" from app_user where login_id = #{loginId}")
  Map<String, Object> findByLoginId(@Param("loginId") String loginId);

  @Select("select u.user_id as \"userId\", u.login_id as \"loginId\", u.display_name as \"displayName\", s.session_id as \"sessionId\" from user_session s join app_user u on u.user_id=s.user_id where s.session_id=#{sessionId} and s.status='ACTIVE' and s.expires_at > now() and u.system_enabled='Y'")
  Map<String, Object> findUserByActiveSession(@Param("sessionId") String sessionId);

  @Select("select role_code from user_role_assignment where user_id=#{userId} and status='ACTIVE' and effective_start_date <= current_date and (effective_end_date is null or effective_end_date >= current_date) order by role_code")
  java.util.List<String> findRoleCodes(@Param("userId") String userId);

  @Select("select m.menu_id as \"menuId\", m.parent_menu_id as \"parentMenuId\", m.menu_name as \"menuName\", m.screen_id as \"screenId\", m.route_path as \"routePath\", m.icon_name as \"iconName\", m.business_category as \"businessCategory\", p.permission_level as \"permissionLevel\" from menu m join menu_permission p on p.menu_id=m.menu_id and p.target_type='ROLE' where p.target_id in (select role_code from user_role_assignment where user_id=#{userId} and status='ACTIVE') and p.permission_level <> 'NONE' and m.is_active='Y' order by m.display_order")
  java.util.List<Map<String, Object>> findGrantedMenus(@Param("userId") String userId);

  @Insert("insert into user_session(session_id,user_id,issued_at,expires_at,status,created_at,updated_at) values(#{sessionId},#{userId},now(),#{expiresAt},'ACTIVE',now(),now())")
  int createSession(@Param("sessionId") String sessionId, @Param("userId") String userId, @Param("expiresAt") java.time.OffsetDateTime expiresAt);

  @Update("update user_session set status='REVOKED', updated_at=now() where session_id=#{sessionId} and status='ACTIVE'")
  int revokeSession(@Param("sessionId") String sessionId);
}
