package kr.ac.knue.facultyassessment.personnel;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public interface PersonnelInformationPort {

    List<PersonnelSnapshot> findPersonnel();

    record PersonnelSnapshot(
        String personnelNo,
        String name,
        String organizationCode,
        String positionName,
        String employmentStatus,
        LocalDate retirementDate,
        OffsetDateTime lastSyncedAt
    ) {
    }
}
