package kr.ac.knue.facultyassessment.personnel;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PersonnelSnapshotMapper {

    @Select("select personnel_no as \"personnelNo\", name, organization_code as \"organizationCode\", position_name as \"positionName\", employment_status as \"employmentStatus\", retirement_date as \"retirementDate\", last_synced_at as \"lastSyncedAt\" from korus_personnel_snapshot order by personnel_no")
    List<PersonnelInformationPort.PersonnelSnapshot> findAll();
}
