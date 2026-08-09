package kr.ac.knue.facultyassessment.menus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class MenuInformationManagementApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanSaveAndRequeryMenuExecutionInformationAndScreenConnection() throws Exception {
        assertMenuInformationContractIsAvailable();
        Cookie session = login("admin", "admin");

        mockMvc.perform(get("/api/menus")
                .cookie(session)
                .param("parentMenuId", "MENU-MANAGEMENT")
                .param("useYn", "Y"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[?(@.menuId == 'MENU-MENU-INFORMATION-MANAGEMENT')].screenId").value("SCR-MENU-INFORMATION-MANAGEMENT"));

        mockMvc.perform(post("/api/menus")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuName\":\"메뉴 정보 관리\",\"parentMenuId\":\"MENU-MANAGEMENT\",\"displayOrder\":2,\"screenId\":\"SCR-MENU-INFORMATION-MANAGEMENT\",\"url\":\"/system/menus/information-updated\",\"icon\":\"menu-file\",\"businessCategory\":\"SYSTEM-MANAGEMENT\",\"description\":\"메뉴 실행정보와 화면 연결을 관리합니다.\",\"useYn\":\"Y\",\"reason\":\"실행 화면 정보 정비\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/menus")
                .cookie(session)
                .param("parentMenuId", "MENU-MANAGEMENT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.menuId == 'MENU-MENU-INFORMATION-MANAGEMENT')].menuName").value("메뉴 정보 관리"))
            .andExpect(jsonPath("$.data[?(@.menuId == 'MENU-MENU-INFORMATION-MANAGEMENT')].screenId").value("SCR-MENU-INFORMATION-MANAGEMENT"))
            .andExpect(jsonPath("$.data[?(@.menuId == 'MENU-MENU-INFORMATION-MANAGEMENT')].url").value("/system/menus/information-updated"))
            .andExpect(jsonPath("$.data[?(@.menuId == 'MENU-MENU-INFORMATION-MANAGEMENT')].icon").value("menu-file"))
            .andExpect(jsonPath("$.data[?(@.menuId == 'MENU-MENU-INFORMATION-MANAGEMENT')].businessCategory").value("SYSTEM-MANAGEMENT"))
            .andExpect(jsonPath("$.data[?(@.menuId == 'MENU-MENU-INFORMATION-MANAGEMENT')].description").value("메뉴 실행정보와 화면 연결을 관리합니다."));

        Assertions.assertEquals(1, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'menu' and entity_id = 'MENU-MENU-INFORMATION-MANAGEMENT' and actor_user_id = 'admin'",
            Integer.class
        ));
    }

    @Test
    void savingInactiveMenuPreservesMenuRowAndRejectsMissingScreenId() throws Exception {
        Cookie session = login("admin", "admin");
        Integer historyBefore = jdbcTemplate.queryForObject("select count(*) from change_history where entity_name = 'menu'", Integer.class);

        mockMvc.perform(post("/api/menus")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuName\":\"메뉴 정보 관리\",\"parentMenuId\":\"MENU-MANAGEMENT\",\"displayOrder\":2,\"url\":\"/system/menus/information\",\"useYn\":\"N\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("screenId"));

        mockMvc.perform(post("/api/menus")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuName\":\"메뉴 정보 관리\",\"parentMenuId\":\"MENU-MANAGEMENT\",\"displayOrder\":2,\"screenId\":\"SCR-MENU-INFORMATION-MANAGEMENT\",\"url\":\"/system/menus/information\",\"useYn\":\"N\",\"reason\":\"메뉴 사용 중지\"}"))
            .andExpect(status().isOk());

        Assertions.assertEquals("N", jdbcTemplate.queryForObject(
            "select use_yn from menu where menu_id = 'MENU-MENU-INFORMATION-MANAGEMENT'", String.class
        ));
        Assertions.assertEquals(1, jdbcTemplate.queryForObject(
            "select count(*) from menu where menu_id = 'MENU-MENU-INFORMATION-MANAGEMENT'", Integer.class
        ));
        Assertions.assertEquals(historyBefore + 1, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'menu'", Integer.class
        ));
    }

    private Cookie login(String userId, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return login.getResponse().getCookie("SESSION");
    }

    private void assertMenuInformationContractIsAvailable() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertTrue(contract.contains("operationId: listMenus"));
        Assertions.assertTrue(contract.contains("operationId: saveMenu"));
        Assertions.assertTrue(contract.contains("screenId"));
        Assertions.assertTrue(contract.contains("useYn=N 메뉴 삭제 요청"));
    }
}
