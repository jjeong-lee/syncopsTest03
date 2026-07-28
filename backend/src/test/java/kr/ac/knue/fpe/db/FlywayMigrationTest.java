package kr.ac.knue.fpe.db;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationTest {
    @Test void schema_contains_required_tables_indexes_and_comments() throws Exception {
        String sql = Files.readString(Path.of("src/main/resources/db/migration/V001__common_foundation_schema.sql"));
        for (String table : new String[]{"user_account","korus_personnel_snapshot","organization","organization_user_mapping","role","user_role","menu","menu_permission","code_group","detail_code","user_session"}) {
            assertThat(sql).contains("CREATE TABLE IF NOT EXISTS " + table, "COMMENT ON TABLE " + table);
        }
        assertThat(sql).contains("CREATE INDEX IF NOT EXISTS idx_user_search", "CREATE INDEX IF NOT EXISTS idx_menu_permission_target");
    }
}
