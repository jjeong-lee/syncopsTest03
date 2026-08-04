package kr.ac.knue.fpe.ports;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import kr.ac.knue.fpe.persistence.ManagementMapper;

@Component
public class MockPersonnelInformationAdapter implements PersonnelInformationPort {
    private final ManagementMapper mapper;
    public MockPersonnelInformationAdapter(ManagementMapper mapper) { this.mapper = mapper; }
    @Override
    public List<Map<String, Object>> searchReadonlyStaff(String staffNo, String staffName, String organizationCode) {
        return mapper.searchReadonlyStaff(staffNo, staffName, organizationCode);
    }
}
