package kr.ac.knue.fpe.admin;

import org.apache.ibatis.jdbc.SQL;
import java.util.Map;

public class AdminSqlProvider {
    private static boolean present(Map<String,Object> p, String key) { Object v = p.get(key); return v != null && !String.valueOf(v).isBlank(); }
    public String listUsers(Map<String,Object> p) {
        SQL sql = new SQL().SELECT("ua.user_id, ua.employee_no, ua.login_id, ua.display_name, ks.organization_code, o.organization_name, ks.job_grade, ks.employment_status, ks.position_name, ks.retirement_date, ks.last_synced_at, ua.system_use_yn, ua.account_status")
            .FROM("user_account ua").LEFT_OUTER_JOIN("korus_personnel_snapshot ks on ks.employee_no=ua.employee_no").LEFT_OUTER_JOIN("organization o on o.organization_code=ks.organization_code");
        if (present(p,"employeeNo")) sql.WHERE("ua.employee_no like concat('%', #{employeeNo}, '%')");
        if (present(p,"name")) sql.WHERE("(ua.display_name like concat('%', #{name}, '%') or ks.person_name like concat('%', #{name}, '%'))");
        if (present(p,"organizationCode")) sql.WHERE("ks.organization_code = #{organizationCode}");
        if (present(p,"jobGrade")) sql.WHERE("ks.job_grade = #{jobGrade}");
        if (present(p,"employmentStatus")) sql.WHERE("ks.employment_status = #{employmentStatus}");
        if (present(p,"systemUseYn")) sql.WHERE("ua.system_use_yn = #{systemUseYn}");
        return sql.toString() + " order by ks.last_synced_at desc limit #{size} offset #{offset}";
    }
    public String listOrganizations(Map<String,Object> p) {
        SQL sql = new SQL().SELECT("organization_id, organization_code, organization_name, organization_type, parent_organization_code, effective_start_date, effective_end_date, use_yn").FROM("organization");
        if (present(p,"organizationCode")) sql.WHERE("organization_code like concat('%', #{organizationCode}, '%')");
        if (present(p,"organizationName")) sql.WHERE("organization_name like concat('%', #{organizationName}, '%')");
        if (present(p,"organizationType")) sql.WHERE("organization_type = #{organizationType}");
        return sql.toString() + " order by organization_code limit #{size} offset #{offset}";
    }
    public String listRoles(Map<String,Object> p) {
        SQL sql = new SQL().SELECT("role_code, role_name, role_purpose, assignment_criteria, default_data_scope, use_yn").FROM("role");
        if (present(p,"roleCode")) sql.WHERE("role_code = #{roleCode}");
        if (present(p,"useYn")) sql.WHERE("use_yn = #{useYn}");
        return sql.toString() + " order by role_code limit #{size} offset #{offset}";
    }
    public String listUserRoles(Map<String,Object> p) {
        SQL sql = new SQL().SELECT("user_role_id, user_id, role_code, role_source, valid_from, valid_to, approved_by_user_id, assignment_status").FROM("user_role");
        if (present(p,"userId")) sql.WHERE("user_id = cast(#{userId} as uuid)");
        if (present(p,"roleCode")) sql.WHERE("role_code = #{roleCode}");
        if (present(p,"roleSource")) sql.WHERE("role_source = #{roleSource}");
        return sql.toString() + " order by valid_from desc limit #{size} offset #{offset}";
    }
    public String listMenus(Map<String,Object> p) {
        SQL sql = new SQL().SELECT("menu_id, parent_menu_id, menu_level, display_order, menu_name, screen_id, url_path, icon_name, business_category, description, use_yn").FROM("menu");
        if (present(p,"menuName")) sql.WHERE("menu_name like concat('%', #{menuName}, '%')");
        if (present(p,"menuLevel")) sql.WHERE("menu_level = #{menuLevel}");
        if (present(p,"useYn")) sql.WHERE("use_yn = #{useYn}");
        return sql.toString() + " order by display_order, menu_name limit #{size} offset #{offset}";
    }
    public String listCodeGroups(Map<String,Object> p) {
        SQL sql = new SQL().SELECT("group_id, group_name, description, managing_department, use_yn").FROM("code_group");
        if (present(p,"groupId")) sql.WHERE("group_id like concat('%', #{groupId}, '%')");
        if (present(p,"groupName")) sql.WHERE("group_name like concat('%', #{groupName}, '%')");
        if (present(p,"managingDepartment")) sql.WHERE("managing_department like concat('%', #{managingDepartment}, '%')");
        if (present(p,"useYn")) sql.WHERE("use_yn = #{useYn}");
        return sql.toString() + " order by group_id limit #{size} offset #{offset}";
    }
    public String listDetailCodes(Map<String,Object> p) {
        SQL sql = new SQL().SELECT("detail_code_id, group_id, code_value, code_name, parent_detail_code_id, sort_order, additional_attributes, use_yn, valid_from, valid_to").FROM("detail_code");
        if (present(p,"groupId")) sql.WHERE("group_id = #{groupId}");
        if (present(p,"codeValue")) sql.WHERE("code_value like concat('%', #{codeValue}, '%')");
        if (present(p,"codeName")) sql.WHERE("code_name like concat('%', #{codeName}, '%')");
        if (present(p,"useYn")) sql.WHERE("use_yn = #{useYn}");
        return sql.toString() + " order by group_id, sort_order limit #{size} offset #{offset}";
    }
}
