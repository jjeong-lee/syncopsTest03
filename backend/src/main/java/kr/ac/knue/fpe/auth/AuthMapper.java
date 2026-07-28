package kr.ac.knue.fpe.auth;

import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.*;

@Mapper
public interface AuthMapper {
    @Select("select user_id, login_id, display_name, password_hash, account_status, system_use_yn from user_account where login_id = #{loginId}")
    Map<String,Object> findAccount(@Param("loginId") String loginId);
    @Select("select r.role_code from user_role ur join role r on r.role_code=ur.role_code where ur.user_id=#{userId} and ur.assignment_status='ACTIVE' and r.use_yn='Y' and current_date between ur.valid_from and coalesce(ur.valid_to, current_date)")
    List<String> roleCodes(@Param("userId") UUID userId);
    @Insert("insert into user_session(session_id,user_id,issued_at,expires_at,same_site_policy,session_status,created_at,updated_at) values(#{sessionId},#{userId},#{issuedAt},#{expiresAt},'Lax','ACTIVE',now(),now())")
    void createSession(@Param("sessionId") String sessionId, @Param("userId") UUID userId, @Param("issuedAt") LocalDateTime issuedAt, @Param("expiresAt") LocalDateTime expiresAt);
    @Select("select ua.user_id, ua.login_id, ua.display_name from user_session s join user_account ua on ua.user_id=s.user_id where s.session_id=#{sessionId} and s.session_status='ACTIVE' and s.expires_at > now() and ua.account_status='ACTIVE' and ua.system_use_yn='Y'")
    Map<String,Object> findBySession(@Param("sessionId") String sessionId);
    @Update("update user_session set session_status='REVOKED', updated_at=now() where session_id=#{sessionId}")
    void revoke(@Param("sessionId") String sessionId);
}
