package kr.ac.knue.fpe;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiFixtureTest {
    @Test
    void openApiFixtureIsAvailableOnClasspath() throws Exception {
        String text = new String(new ClassPathResource("contracts/openapi.yaml").getInputStream().readAllBytes());
        assertThat(text).contains("operationId: listUsers", "operationId: updateDetailCode");
    }
}
