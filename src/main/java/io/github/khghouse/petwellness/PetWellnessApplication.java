package io.github.khghouse.petwellness;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PetWellnessApplication {

    public static void main(String[] args) {
        configureDefaultTimeZone();
        SpringApplication.run(PetWellnessApplication.class, args);
    }

    static void configureDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
