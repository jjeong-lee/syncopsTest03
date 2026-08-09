package kr.ac.knue.facultyassessment.detailcodes;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class DetailCodeManagementApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void r09CanCreateUpdateAndRequeryDetailCodeHierarchyAndAdditionalAttributes() throws Exception {
        assertDetailCodeContractIsAvailable();
        Cookie session = login("admin", "admin");
        createCodeGroup(session, "CG-TEST-DETAIL");

        mockMvc.perform(post("/api/code-groups/{groupId}/detail-codes", "CG-TEST-DETAIL")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeValue\":\"PARENT\",\"codeName\":\"상위 코드\",\"displayOrder\":1,\"additionalAttributes\":{\"mappingKey\":\"PARENT-MAP\"},\"useYn\":\"Y\",\"reason\":\"상위 코드 등록\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        String parentDetailCodeId = jdbcTemplate.queryForObject(
            "select detail_code_id from detail_code where group_id = 'CG-TEST-DETAIL' and code_value = 'PARENT'",
            String.class
        );

        mockMvc.perform(post("/api/code-groups/{groupId}/detail-codes", "CG-TEST-DETAIL")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeValue\":\"CHILD\",\"codeName\":\"하위 코드\",\"parentDetailCodeId\":\"" + parentDetailCodeId + "\",\"displayOrder\":2,\"additionalAttributes\":{\"mappingKey\":\"CHILD-MAP\",\"externalCode\":\"EXT-01\"},\"useYn\":\"Y\",\"reason\":\"하위 코드 등록\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/code-groups/{groupId}/detail-codes", "CG-TEST-DETAIL")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeValue\":\"CHILD\",\"codeName\":\"수정된 하위 코드\",\"parentDetailCodeId\":\"" + parentDetailCodeId + "\",\"displayOrder\":3,\"additionalAttributes\":{\"mappingKey\":\"CHILD-MAP-UPDATED\"},\"useYn\":\"Y\",\"reason\":\"정렬순서와 연계 속성 변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/code-groups/{groupId}/detail-codes", "CG-TEST-DETAIL")
                .cookie(session)
                .param("useYn", "Y"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[1].codeValue").value("CHILD"))
            .andExpect(jsonPath("$.data[1].codeName").value("수정된 하위 코드"))
            .andExpect(jsonPath("$.data[1].parentDetailCodeId").value(parentDetailCodeId))
            .andExpect(jsonPath("$.data[1].displayOrder").value(3))
            .andExpect(jsonPath("$.data[1].additionalAttributes.mappingKey").value("CHILD-MAP-UPDATED"));

        Assertions.assertEquals(3, jdbcTemplate.queryForObject(
            "select count(*) from change_history where entity_name = 'detail_code' and actor_user_id = 'admin'",
            Integer.class
        ));
    }

    @Test
    void invalidCodeValueIsRejectedAndInactiveDetailCodeRowIsPreserved() throws Exception {
        Cookie session = login("admin", "admin");
        createCodeGroup(session, "CG-TEST-INACTIVE");

        mockMvc.perform(post("/api/code-groups/{groupId}/detail-codes", "CG-TEST-INACTIVE")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeName\":\"입력 오류\",\"displayOrder\":1}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.field").value("codeValue"));

        mockMvc.perform(post("/api/code-groups/{groupId}/detail-codes", "CG-TEST-INACTIVE")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeValue\":\"INACTIVE\",\"codeName\":\"비활성 대상\",\"displayOrder\":1,\"useYn\":\"Y\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/code-groups/{groupId}/detail-codes", "CG-TEST-INACTIVE")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeValue\":\"INACTIVE\",\"codeName\":\"비활성 대상\",\"displayOrder\":1,\"useYn\":\"N\",\"reason\":\"사용 중지\"}"))
            .andExpect(status().isOk());

        Assertions.assertEquals("N", jdbcTemplate.queryForObject(
            "select use_yn from detail_code where group_id = 'CG-TEST-INACTIVE' and code_value = 'INACTIVE'",
            String.class
        ));
        Assertions.assertEquals(1, jdbcTemplate.queryForObject(
            "select count(*) from detail_code where group_id = 'CG-TEST-INACTIVE' and code_value = 'INACTIVE'",
            Integer.class
        ));
    }

    @Test
    void nonR09SessionCannotSaveDetailCode() throws Exception {
        Cookie session = login("member", "member");

        mockMvc.perform(post("/api/code-groups/{groupId}/detail-codes", "CG-FORBIDDEN")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeValue\":\"FORBIDDEN\",\"codeName\":\"권한 없는 등록\",\"displayOrder\":1}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    private void createCodeGroup(Cookie session, String groupId) throws Exception {
        mockMvc.perform(post("/api/code-groups")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"" + groupId + "\",\"groupName\":\"상세코드 테스트 그룹\"}"))
            .andExpect(status().isOk());
    }

    private Cookie login(String userId, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return login.getResponse().getCookie("SESSION");
    }

    private void assertDetailCodeContractIsAvailable() throws Exception {
        String contract = new org.springframework.core.io.ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        Assertions.assertTrue(contract.contains("operationId: listDetailCodes"));
        Assertions.assertTrue(contract.contains("operationId: saveDetailCode"));
        Assertions.assertTrue(contract.contains("parentDetailCodeId"));
        Assertions.assertTrue(contract.contains("additionalAttributes"));
        Assertions.assertTrue(contract.contains("change_history에 상세코드 변경을 기록한다"));
    }
}
