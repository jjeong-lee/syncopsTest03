package kr.ac.knue.facultyassessment.personnel;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class KorusMockSnapshotAdapter implements PersonnelInformationPort {

    private final PersonnelSnapshotMapper personnelSnapshotMapper;

    public KorusMockSnapshotAdapter(PersonnelSnapshotMapper personnelSnapshotMapper) {
        this.personnelSnapshotMapper = personnelSnapshotMapper;
    }

    @Override
    public List<PersonnelSnapshot> findPersonnel() {
        return personnelSnapshotMapper.findAll();
    }
}
