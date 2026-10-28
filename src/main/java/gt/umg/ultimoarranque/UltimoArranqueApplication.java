package gt.umg.ultimoarranque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UltimoArranqueApplication {

    public static void main(String[] args) {
        SpringApplication.run(UltimoArranqueApplication.class, args);
    }
}
