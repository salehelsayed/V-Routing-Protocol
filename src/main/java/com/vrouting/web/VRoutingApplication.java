package com.vrouting.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.vrouting"})
public class VRoutingApplication {
    public static void main(String[] args) {
        SpringApplication.run(VRoutingApplication.class, args);
    }
}
