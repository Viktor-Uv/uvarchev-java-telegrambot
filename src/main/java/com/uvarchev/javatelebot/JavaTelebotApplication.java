package com.uvarchev.javatelebot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JavaTelebotApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaTelebotApplication.class, args);
    }

}
