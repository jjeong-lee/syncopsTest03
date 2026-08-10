package kr.ac.knue.facultyassessment.codegroups;

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
class CodeGroupManagementApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanCreateUpdateAndRequeryCodeGroupFields() throws Exception {
        assertCodeGroupContractIsAvailable();
        Cookie session = login("admin", "admin");

        mockMvc.perform(post("/api/code-groups")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"CG-TEST-ACADEMIC\",\"groupName\":\"학사 상태\",\"description\":\"학사 상태 공통코드\",\"managementDepartment\":\"교수지원과\",\"useYn\":\"Y\",\"reason\":\"코드그룹 등록\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/code-groups")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"CG-TEST-ACADEMIC\",\"groupName\":\"학사 상태 코드\",\"description\":\"수정된 학사 상태 공통코드\",\"managementDepartment\":\"학사관리과\",\"useYn\":\"Y\",\"reason\":\"관리부서 변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/code-groups")
                .cookie(session)
                .param("groupId", "CG-TEST-ACADEMIC")
                .param("useYn", "Y"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].groupId").value("CG-TEST-ACADEMIC"))
            .andExpect(jsonPath("$.data[0].groupName").value("학사 상태 코드"))
            .andExpect(jsonPath("$.data[0].description").value("수정된 학사 상태 공통코드"))
            .andExpect(jsonPath("$.data[0].managementDepartment").value("학사관리과"))
            .andExpect(jsonPath("$.data[0].useYn").value("Y"));

        Assertions.assertEquals(2, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'code_group' and entity_id = 'CG-TEST-ACADEMIC' and actor_user_id = 'admin'",
            Integer.class
        ));
    }

    @Test
    void invalidGroupIdIsRejectedAndInactiveCodeGroupRowIsPreserved() throws Exception {
        Cookie session = login("admin", "admin");

        mockMvc.perform(post("/api/code-groups")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\":\"입력 오류\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("groupId"));

        mockMvc.perform(post("/api/code-groups")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"CG-TEST-INACTIVE\",\"groupName\":\"비활성 대상\",\"useYn\":\"Y\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/code-groups")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"CG-TEST-INACTIVE\",\"groupName\":\"비활성 대상\",\"useYn\":\"N\",\"reason\":\"사용 중지\"}"))
            .andExpect(status().isOk());

        Assertions.assertEquals("N", jdbcTemplate.queryForObject(
            "select use_yn from code_group where group_id = 'CG-TEST-INACTIVE'", String.class
        ));
        Assertions.assertEquals(1, jdbcTemplate.queryForObject(
            "select count(*) from code_group where group_id = 'CG-TEST-INACTIVE'", Integer.class
        ));
        Assertions.assertEquals(2, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'code_group' and entity_id = 'CG-TEST-INACTIVE' and actor_user_id = 'admin'",
            Integer.class
        ));
    }

    @Test
    void nonR09SessionCannotSaveCodeGroup() throws Exception {
        Cookie session = login("member", "member");

        mockMvc.perform(post("/api/code-groups")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"CG-FORBIDDEN\",\"groupName\":\"권한 없는 등록\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

        Assertions.assertEquals(0, jdbcTemplate.queryForObject(
            "select count(*) from code_group where group_id = 'CG-FORBIDDEN'",
            Integer.class
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

    private void assertCodeGroupContractIsAvailable() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertTrue(contract.contains("operationId: listCodeGroups"));
        Assertions.assertTrue(contract.contains("operationId: saveCodeGroup"));
        Assertions.assertTrue(contract.contains("managementDepartment"));
        Assertions.assertTrue(contract.contains("change_history에 코드그룹 변경을 기록한다"));
    }
}
