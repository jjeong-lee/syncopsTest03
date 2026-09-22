package kr.ac.knue.facultyassessment.setting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ChangeBoundaryTest {

    private static final Path CHANGE_MIGRATION = Path.of(
        "src/main/resources/db/migration/V5__usage_and_setting_management.sql"
    );

    @Test
    void setupMigrationAddsOnlyUsageAndSettingPersistenceWithoutExcludedExecutions() throws IOException {
        String migration = Files.readString(CHANGE_MIGRATION).toUpperCase(Locale.ROOT);

        assertTrue(migration.contains("ALTER TABLE MENU"));
        assertTrue(migration.contains("ALTER TABLE DETAIL_CODE"));
        assertTrue(migration.contains("CREATE TABLE IF NOT EXISTS COMMON_ENVIRONMENT_SETTING"));
        assertTrue(migration.contains("CREATE TABLE IF NOT EXISTS REFERENCE_YEAR_SETTING"));

        assertFalse(migration.contains("DELETE FROM"));
        assertFalse(migration.contains("TRUNCATE TABLE"));
        assertFalse(migration.contains("DROP TABLE"));
        assertFalse(migration.contains("CREATE " + "BATCH"));
        assertFalse(migration.contains("COPY "));
        assertFalse(migration.contains("INITIALIZE"));
    }
}
