package kr.ac.knue.fpe;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ScopeGuardTest {
  @Test
  void excludedBusinessFileExcelAuditBatchApisAreNotGenerated() throws Exception {
    String source = Files.walk(Path.of("src/main/java"))
        .filter(Files::isRegularFile)
        .map(path -> {
          try { return Files.readString(path); } catch (Exception e) { return ""; }
        })
        .collect(Collectors.joining("\n"));
    assertThat(source).doesNotContain("/api/achievements", "/api/files", "/api/excel", "/api/audit-logs", "/api/batches");
  }
}
