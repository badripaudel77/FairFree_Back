package com.app.fairfree;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FairFreeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FairFreeApplication.class, args);
    }

}
