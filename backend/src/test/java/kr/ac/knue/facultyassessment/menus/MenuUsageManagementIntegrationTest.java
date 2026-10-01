package kr.ac.knue.facultyassessment.menus;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class MenuUsageManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void r09CanSaveAndRequeryMenuUsageAndExposurePeriod() throws Exception {
        Cookie session = login("admin", "admin");

        mockMvc.perform(post("/api/menus")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuName\":\"메뉴 사용 관리\",\"parentMenuId\":\"MENU-MANAGEMENT\",\"displayOrder\":3,\"screenId\":\"SCR-MENU-USAGE-MANAGEMENT\",\"url\":\"/system/menus/usage\",\"useYn\":\"Y\",\"exposureStartAt\":\"2026-10-01T00:00:00Z\",\"exposureEndAt\":null,\"reason\":\"메뉴 사용 기간 설정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/menus").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.menuId == 'MENU-MENU-USAGE-MANAGEMENT')].useYn").value("Y"))
            .andExpect(jsonPath("$.data[?(@.menuId == 'MENU-MENU-USAGE-MANAGEMENT')].exposureStartAt").value("2026-10-01T00:00:00Z"))
            .andExpect(jsonPath("$.data[?(@.menuId == 'MENU-MENU-USAGE-MANAGEMENT')].exposureEndAt").isEmpty());

        mockMvc.perform(get("/api/auth/me").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.menus[?(@.menuId == 'MENU-MENU-USAGE-MANAGEMENT')].route").value("/system/menus/usage"));
    }

    @Test
    void inactiveOrOutOfPeriodUsageMenuIsBlockedAtTheDirectRouteWithoutPersistingACommand() throws Exception {
        Cookie session = login("admin", "admin");

        mockMvc.perform(post("/api/menus")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuName\":\"메뉴 사용 관리\",\"parentMenuId\":\"MENU-MANAGEMENT\",\"displayOrder\":3,\"screenId\":\"SCR-MENU-USAGE-MANAGEMENT\",\"url\":\"/system/menus/usage\",\"useYn\":\"N\",\"exposureStartAt\":\"2026-10-01T00:00:00Z\",\"exposureEndAt\":null}"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/system/menus/usage").cookie(session))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
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
}
