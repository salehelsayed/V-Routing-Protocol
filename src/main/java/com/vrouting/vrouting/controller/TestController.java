package com.vrouting.vrouting.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    public TestController() {
        logger.info("TestController initialized");
    }

    @GetMapping("/test")
    public String test() {
        logger.info("Test endpoint called");
        return "API is working";
    }

    @GetMapping("/error")
    public String error() {
        try {
            throw new RuntimeException("Simulated error");
        } catch (Exception e) {
            logger.error("An error occurred: {}", e.getMessage());
            return "Error occurred";
        }
    }
}
