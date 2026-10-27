package kr.ac.knue.facultyassessment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SupplyChainSecurityContractTest {

    @Test
    void securityCriticalImagesAndEmbeddedServerDependenciesUsePatchedVersions() throws IOException {
        String pom = Files.readString(Path.of("pom.xml"));
        String backendDockerfile = Files.readString(Path.of("Dockerfile"));
        assertTrue(pom.contains("<version>4.0.3</version>"));
        assertTrue(pom.contains("<tomcat.version>11.0.25</tomcat.version>"));
        assertTrue(pom.contains("<spring-framework.version>7.0.9</spring-framework.version>"));
        assertTrue(pom.contains("<junit-jupiter.version>6.0.3</junit-jupiter.version>"));
        assertTrue(backendDockerfile.contains("eclipse-temurin:17.0.17_10-jre-alpine"));
        assertTrue(backendDockerfile.contains("RUN apk upgrade --no-cache"));
    }
}
