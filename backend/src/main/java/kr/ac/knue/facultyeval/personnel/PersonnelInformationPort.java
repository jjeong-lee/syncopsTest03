package kr.ac.knue.facultyeval.personnel;

import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PersonnelInformationPort {
  @Select("select staff_no as \"staffNo\", staff_name as \"staffName\", org_code as \"orgCode\", rank_name as \"rankName\", employment_status as \"employmentStatus\", retirement_date as \"retirementDate\", last_synced_at as \"lastSyncedAt\" from korus_staff_snapshot where staff_no=#{staffNo}")
  Map<String, Object> findStaffSnapshot(String staffNo);

  @Select("select org_code as \"orgCode\", org_name as \"orgName\", parent_org_code as \"parentOrgCode\", org_type as \"orgType\" from korus_org_snapshot where org_code=#{orgCode}")
  Map<String, Object> findOrganizationSnapshot(String orgCode);
}
