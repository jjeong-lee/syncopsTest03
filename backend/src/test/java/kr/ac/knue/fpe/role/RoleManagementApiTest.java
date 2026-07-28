package kr.ac.knue.fpe.role;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

class RoleManagementApiTest {
    @Test void openapi_contract_declares_required_operations() throws Exception {
        String yaml = new String(new ClassPathResource("contracts/openapi.yaml").getInputStream().readAllBytes());
        assertThat(yaml).contains("listRoles / GET `/api/roles`", "createRole / POST `/api/roles`", "updateRole");
    }
}
