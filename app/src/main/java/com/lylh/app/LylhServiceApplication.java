package com.lylh.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.lylh.*"})
public class LylhServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LylhServiceApplication.class, args);
    }
}
