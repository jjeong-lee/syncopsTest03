package kr.ac.knue.facultyassessment.externalintegrations;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class SchoolInformationService {

    private final SchoolInformationPort schoolInformationPort;

    public SchoolInformationService(SchoolInformationPort schoolInformationPort) {
        this.schoolInformationPort = schoolInformationPort;
    }

    public List<SchoolInformationPort.SchoolInformation> findSchools(
        String schoolName,
        String educationOfficeCode,
        int pIndex,
        int pSize
    ) {
        return schoolInformationPort.lookup(new SchoolInformationPort.SchoolInformationQuery(
            schoolName,
            educationOfficeCode,
            pIndex,
            pSize
        ));
    }
}
