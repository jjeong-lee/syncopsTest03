package kr.ac.knue.facultyassessment.menus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class MenuStructureManagementApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanRequeryHierarchyAfterChangingParentAndSameLevelDisplayOrder() throws Exception {
        assertMenuStructureContractIsAvailable();
        Cookie session = login("admin", "admin");

        mockMvc.perform(get("/api/menus").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[?(@.menuId == 'MENU-USER-MANAGEMENT')].parentMenuId").value("MENU-USER-ORGANIZATION"));

        mockMvc.perform(post("/api/menus")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuName\":\"사용자 관리\",\"parentMenuId\":\"MENU-ROLES-PERMISSIONS\",\"displayOrder\":1,\"screenId\":\"SCR-USER-MANAGEMENT\",\"url\":\"/system/user-organization/users\",\"useYn\":\"Y\",\"reason\":\"메뉴 구조 조정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/menus")
                .cookie(session)
                .param("parentMenuId", "MENU-ROLES-PERMISSIONS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.menuId == 'MENU-USER-MANAGEMENT')].parentMenuId").value("MENU-ROLES-PERMISSIONS"));

        mockMvc.perform(put("/api/menus/order")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuId\":\"MENU-USER-MANAGEMENT\",\"displayOrder\":0,\"reason\":\"동일 계층 우선 순위 변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/menus")
                .cookie(session)
                .param("parentMenuId", "MENU-ROLES-PERMISSIONS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuId").value("MENU-USER-MANAGEMENT"))
            .andExpect(jsonPath("$.data[0].displayOrder").value(0));

        Assertions.assertEquals(2, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'menu' and entity_id = 'MENU-USER-MANAGEMENT' and actor_user_id = 'admin'",
            Integer.class
        ));
    }

    @Test
    void menuStructureCommandsRejectMissingDisplayOrderAndUnknownMenuWithoutWritingHistory() throws Exception {
        Cookie session = login("admin", "admin");
        Integer historyBefore = jdbcTemplate.queryForObject("select count(*) from change_history where entity_name = 'menu'", Integer.class);

        mockMvc.perform(put("/api/menus/order")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuId\":\"MENU-USER-MANAGEMENT\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("displayOrder"));

        mockMvc.perform(put("/api/menus/order")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuId\":\"MENU-NOT-FOUND\",\"displayOrder\":1}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("menuId"));

        Assertions.assertEquals(historyBefore, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'menu'",
            Integer.class
        ));
    }

    @Test
    void nonAdministratorCannotChangeMenuStructure() throws Exception {
        Cookie session = login("member", "member");

        mockMvc.perform(put("/api/menus/order")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuId\":\"MENU-USER-MANAGEMENT\",\"displayOrder\":1}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    private Cookie login(String userId, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return login.getResponse().getCookie("SESSION");
    }

    private void assertMenuStructureContractIsAvailable() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertTrue(contract.contains("/menus:"));
        Assertions.assertTrue(contract.contains("operationId: listMenus"));
        Assertions.assertTrue(contract.contains("operationId: saveMenu"));
        Assertions.assertTrue(contract.contains("operationId: reorderMenu"));
        Assertions.assertTrue(contract.contains("MenuOrderRequest:"));
    }
}
