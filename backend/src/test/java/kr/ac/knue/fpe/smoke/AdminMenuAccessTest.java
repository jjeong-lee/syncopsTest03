package kr.ac.knue.fpe.smoke;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

class AdminMenuAccessTest {
    @Test void seed_data_defines_r09_and_nine_management_leaf_routes() throws Exception {
        String seed = Files.readString(Path.of("src/main/resources/db/migration/V002__seed_common_foundation_data.sql"));
        assertThat(seed).contains("'R09','시스템관리자'");
        for (String route : new String[]{"/system/users","/system/organizations","/system/roles","/system/user-roles","/system/menu-permissions","/system/menu-structure","/system/menu-info","/system/code-groups","/system/codes"}) {
            assertThat(seed).contains(route);
        }
    }
}
