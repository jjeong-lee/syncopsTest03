package kr.ac.knue.facultyassessment.foundation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PersistenceFoundationContractTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void migrationCreatesRequiredDomainsWithAuditAndStateColumns() {
        List<String> requiredTables = List.of(
            "user_account", "korus_personnel_snapshot", "organization", "organization_relationship",
            "organization_user_mapping", "role", "user_role", "menu", "menu_permission", "code_group",
            "detail_code", "user_session", "change_history"
        );

        for (String table : requiredTables) {
            Integer tableCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_schema = current_schema() and table_name = ?",
                Integer.class,
                table
            );
            org.junit.jupiter.api.Assertions.assertEquals(1, tableCount, table);

            Integer createdAt = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_schema = current_schema() "
                    + "and table_name = ? and column_name = 'created_at'",
                Integer.class,
                table
            );
            Integer updatedAt = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_schema = current_schema() "
                    + "and table_name = ? and column_name = 'updated_at'",
                Integer.class,
                table
            );
            org.junit.jupiter.api.Assertions.assertEquals(1, createdAt, table + ".created_at");
            org.junit.jupiter.api.Assertions.assertEquals(1, updatedAt, table + ".updated_at");
        }

        assertColumnExists("menu", "use_yn");
        assertColumnExists("code_group", "use_yn");
        assertColumnExists("detail_code", "use_yn");
    }

    @Test
    void seededAdminCanLoginWithR09AndSeededFoundationDataExists() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleCodes[0]").value("R09"));

        Integer roleCount = jdbcTemplate.queryForObject("select count(*) from role where role_code between 'R01' and 'R09'", Integer.class);
        Integer systemMenuCount = jdbcTemplate.queryForObject("select count(*) from menu where use_yn = 'Y'", Integer.class);
        Integer organizationCount = jdbcTemplate.queryForObject("select count(*) from organization", Integer.class);
        Integer sampleUserCount = jdbcTemplate.queryForObject("select count(*) from user_account where user_id = 'member'", Integer.class);

        org.junit.jupiter.api.Assertions.assertEquals(9, roleCount);
        org.junit.jupiter.api.Assertions.assertTrue(systemMenuCount >= 12);
        org.junit.jupiter.api.Assertions.assertTrue(organizationCount >= 1);
        org.junit.jupiter.api.Assertions.assertEquals(1, sampleUserCount);
    }

    private void assertColumnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
            "select count(*) from information_schema.columns where table_schema = current_schema() "
                + "and table_name = ? and column_name = ?",
            Integer.class,
            tableName,
            columnName
        );
        org.junit.jupiter.api.Assertions.assertEquals(1, count, tableName + "." + columnName);
    }
}
