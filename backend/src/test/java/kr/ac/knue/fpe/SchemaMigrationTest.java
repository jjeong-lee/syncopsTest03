package kr.ac.knue.fpe;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SchemaMigrationTest {
  @Test
  void schemaDefinesRequiredTablesCommonLifecycleColumnsAndBusinessComments() throws Exception {
    String sql = Files.readString(Path.of("src/main/resources/db/migration/V001__create_common_foundation_schema.sql"));
    for (String table : new String[] {"app_user", "korus_staff_snapshot", "organization", "staff_assignment", "role", "user_role_assignment", "menu", "menu_permission", "code_group", "detail_code", "user_session", "change_history"}) {
      assertThat(sql).contains("CREATE TABLE IF NOT EXISTS " + table);
      assertThat(sql).contains("COMMENT ON TABLE " + table);
    }
    assertThat(sql).contains("created_at timestamptz").contains("updated_at timestamptz");
    assertThat(sql).contains("COMMENT ON COLUMN app_user.account_status IS 'ACTIVE:활성|INACTIVE:비활성|LOCKED:잠금'");
  }
}
