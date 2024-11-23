package com.vrouting.vrouting.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
}
