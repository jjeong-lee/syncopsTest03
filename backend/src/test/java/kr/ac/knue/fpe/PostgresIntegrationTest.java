package kr.ac.knue.fpe;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

abstract class PostgresIntegrationTest {
    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        String datasourceUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (datasourceUrl != null && !datasourceUrl.isBlank()) {
            registry.add("spring.datasource.url", () -> datasourceUrl);
        }

        String datasourceUsername = System.getenv("SPRING_DATASOURCE_USERNAME");
        if (datasourceUsername != null && !datasourceUsername.isBlank()) {
            registry.add("spring.datasource.username", () -> datasourceUsername);
        }

        String datasourcePassword = System.getenv("SPRING_DATASOURCE_PASSWORD");
        if (datasourcePassword != null && !datasourcePassword.isBlank()) {
            registry.add("spring.datasource.password", () -> datasourcePassword);
        }
    }
}
