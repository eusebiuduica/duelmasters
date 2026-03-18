package org.example.duelmasters;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DuelmastersApplication {

    public static void main(String[] args) {
        SpringApplication.run(DuelmastersApplication.class, args);
    }

}
