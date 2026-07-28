package kr.ac.knue.fpe.code;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

class DetailCodeApiTest {
    @Test void openapi_contract_declares_required_operations() throws Exception {
        String yaml = new String(new ClassPathResource("contracts/openapi.yaml").getInputStream().readAllBytes());
        assertThat(yaml).contains("listDetailCodes / GET `/api/detail-codes`", "createDetailCode", "updateDetailCode");
    }
}
