package com.service.logaggregator.controller;

import com.service.logaggregator.service.LogProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class LogIngestionController {

    private final LogProcessingService logProcessingService;

    @Autowired
    public LogIngestionController(LogProcessingService logProcessingService) {
        this.logProcessingService = logProcessingService;
    }


    @PostMapping(value = "/logs/{logSource}", consumes = "text/plain")
    public ResponseEntity<Void> ingestLog(
            @PathVariable String logSource,
            @RequestBody String rawLog) {

        if (logSource == null || logSource.isBlank() || rawLog == null || rawLog.isBlank()) {
            return ResponseEntity.badRequest().build();
        }


        logProcessingService.processAndQueueLog(rawLog, logSource);

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
