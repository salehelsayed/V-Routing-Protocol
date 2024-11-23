package com.vrouting.vrouting.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class LoggingController {

    @GetMapping("/logs")
    public List<String> getLogs() throws Exception {
        return Files.readAllLines(Paths.get("logs/application.log"));
    }
} 