package com.projeto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OhajikiGame {

    public static void main(String[] args) {
        SpringApplication.run(OhajikiGame.class, args);
    }

}
