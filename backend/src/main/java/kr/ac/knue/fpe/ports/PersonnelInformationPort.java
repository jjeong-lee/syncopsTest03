package kr.ac.knue.fpe.ports;

import java.util.List;
import java.util.Map;

public interface PersonnelInformationPort {
    List<Map<String, Object>> searchReadonlyStaff(String staffNo, String staffName, String organizationCode);
}
