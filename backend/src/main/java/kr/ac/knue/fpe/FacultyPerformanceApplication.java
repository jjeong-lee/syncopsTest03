package kr.ac.knue.fpe;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("kr.ac.knue.fpe.common")
public class FacultyPerformanceApplication {
  public static void main(String[] args) {
    SpringApplication.run(FacultyPerformanceApplication.class, args);
  }
}
