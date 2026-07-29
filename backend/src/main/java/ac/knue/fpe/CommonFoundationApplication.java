package ac.knue.fpe;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("ac.knue.fpe")
public class CommonFoundationApplication {
  public static void main(String[] args) {
    SpringApplication.run(CommonFoundationApplication.class, args);
  }
}
