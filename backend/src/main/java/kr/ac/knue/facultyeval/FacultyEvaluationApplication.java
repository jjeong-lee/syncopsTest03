package kr.ac.knue.facultyeval;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("kr.ac.knue.facultyeval")
public class FacultyEvaluationApplication {
  public static void main(String[] args) {
    SpringApplication.run(FacultyEvaluationApplication.class, args);
  }
}
