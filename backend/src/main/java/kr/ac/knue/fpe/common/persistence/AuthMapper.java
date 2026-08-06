package kr.ac.knue.fpe.common.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AuthMapper {
    @Select("select count(*) from user_account where login_id = #{loginId} and password_hash = #{passwordHash} and system_use_yn = 'Y'")
    int validLogin(@Param("loginId") String loginId, @Param("passwordHash") String passwordHash);

    @Insert("insert into session(session_id, login_id, state) values(#{sessionId}, #{loginId}, 'ACTIVE')")
    void createSession(@Param("sessionId") String sessionId, @Param("loginId") String loginId);

    @Select("select login_id from session where session_id = #{sessionId} and state = 'ACTIVE'")
    String findLoginIdBySession(@Param("sessionId") String sessionId);

    @Update("update session set state = 'LOGGED_OUT', updated_at = current_timestamp where session_id = #{sessionId}")
    int logout(@Param("sessionId") String sessionId);

    @Insert("insert into session_termination_history(session_id, login_id, termination_type) values(#{sessionId}, #{loginId}, 'LOGOUT')")
    void recordLogout(@Param("sessionId") String sessionId, @Param("loginId") String loginId);
}
