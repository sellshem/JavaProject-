package kz.qazaqlearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class QazaqLearnApplication {
    public static void main(String[] args) {
        SpringApplication.run(QazaqLearnApplication.class, args);
    }
}
