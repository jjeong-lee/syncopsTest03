package kr.ac.knue.fpe.menu;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

class MenuStructureApiTest {
    @Test void openapi_contract_declares_required_operations() throws Exception {
        String yaml = new String(new ClassPathResource("contracts/openapi.yaml").getInputStream().readAllBytes());
        assertThat(yaml).contains("getMenuTree / GET `/api/menus/tree`", "reorderMenus", "updateMenu");
    }
}
