package kr.ac.knue.facultyassessment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class BackendDockerfileContractTest {

    @Test
    void runtimeImageDeclaresHealthcheckAgainstTheApplicationHealthEndpoint() throws IOException {
        String dockerfile = Files.readString(Path.of("Dockerfile"));

        assertTrue(dockerfile.contains("HEALTHCHECK"));
        assertTrue(dockerfile.contains("http://localhost:8080/api/health"));
    }
}
