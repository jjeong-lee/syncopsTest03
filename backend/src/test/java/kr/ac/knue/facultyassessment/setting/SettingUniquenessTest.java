package kr.ac.knue.facultyassessment.setting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class SettingUniquenessTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SettingUniquenessRepository settingUniquenessRepository;

    @Test
    void menuAndDetailCodeKeysAreBackedByOnePersistedConfigurationRow() {
        jdbcTemplate.update(
            "insert into code_group (group_id, group_name) values ('GROUP-UNIQUE-TEST', '유일성 테스트 코드그룹')"
        );
        jdbcTemplate.update(
            "insert into detail_code (detail_code_id, group_id, code_value, code_name, display_order, effective_start_date) values ('DETAIL-UNIQUE-TEST', 'GROUP-UNIQUE-TEST', 'ACTIVE', '활성', 1, current_date)"
        );

        assertTrue(settingUniquenessRepository.menuExists("MENU-POSITION-ASSIGNMENT-MANAGEMENT"));
        assertTrue(settingUniquenessRepository.detailCodeKeyExists("GROUP-UNIQUE-TEST", "ACTIVE"));
        assertUniqueIndexOrConstraint("menu", "menu_id");
        assertUniqueIndexOrConstraint("detail_code", "group_id");
        assertUniqueIndexOrConstraint("detail_code", "code_value");
        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
            "insert into detail_code (detail_code_id, group_id, code_value, code_name, display_order, effective_start_date) values ('DETAIL-UNIQUE-DUPLICATE', 'GROUP-UNIQUE-TEST', 'ACTIVE', '중복 활성', 2, current_date)"
        ));
    }

    @Test
    void menuKeyRejectsDuplicatePersistence() {
        assertTrue(settingUniquenessRepository.menuExists("MENU-POSITION-ASSIGNMENT-MANAGEMENT"));
        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
            "insert into menu (menu_id, menu_name, display_order, use_yn, exposure_start_at) values ('MENU-POSITION-ASSIGNMENT-MANAGEMENT', '중복 메뉴', 99, 'Y', current_timestamp)"
        ));
    }

    @Test
    void commonEnvironmentSettingKeyRejectsDuplicatePersistence() {
        jdbcTemplate.update(
            "insert into common_environment_setting (common_environment_setting_id, setting_key, setting_value) values ('SETTING-TEST', 'SESSION_IDLE_MINUTES', '30')"
        );

        assertTrue(settingUniquenessRepository.commonEnvironmentSettingKeyExists("SESSION_IDLE_MINUTES"));
        assertUniqueIndexOrConstraint("common_environment_setting", "setting_key");
        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
            "insert into common_environment_setting (common_environment_setting_id, setting_key, setting_value) values ('SETTING-DUPLICATE', 'SESSION_IDLE_MINUTES', '60')"
        ));
    }

    @Test
    void referenceYearConfigurationRejectsDuplicatePersistence() {
        jdbcTemplate.update(
            "insert into reference_year_setting (reference_year_setting_id, current_evaluation_year, default_query_year) values ('REFERENCE-CONFIG-TEST', 2026, 2026)"
        );

        assertTrue(settingUniquenessRepository.activeReferenceYearConfigurationExists());
        assertTrue(indexDefinitionExists("uq_reference_year_setting_configuration"));
        assertTrue(indexDefinitionExists("uq_reference_year_setting_target_year"));
        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
            "insert into reference_year_setting (reference_year_setting_id, current_evaluation_year, default_query_year) values ('REFERENCE-CONFIG-DUPLICATE', 2027, 2027)"
        ));
    }

    @Test
    void referenceYearTargetRejectsDuplicatePersistence() {
        jdbcTemplate.update(
            "insert into reference_year_setting (reference_year_setting_id, target_year, baseline_copy_yn, initialization_yn) values ('REFERENCE-TARGET-TEST', 2027, '예', '아니오')"
        );

        assertTrue(settingUniquenessRepository.activeReferenceYearTargetExists(2027));
        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
            "insert into reference_year_setting (reference_year_setting_id, target_year, baseline_copy_yn, initialization_yn) values ('REFERENCE-TARGET-DUPLICATE', 2027, '아니오', '예')"
        ));
    }

    private void assertUniqueIndexOrConstraint(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
            "select count(*) from pg_indexes where schemaname = current_schema() and tablename = ? and indexdef like ?",
            Integer.class,
            tableName,
            "%UNIQUE%" + columnName + "%"
        );
        assertFalse(count == null || count == 0, tableName + "." + columnName + " must be unique");
    }

    private boolean indexDefinitionExists(String indexName) {
        Integer count = jdbcTemplate.queryForObject(
            "select count(*) from pg_indexes where schemaname = current_schema() and indexname = ?",
            Integer.class,
            indexName
        );
        return count != null && count == 1;
    }
}
