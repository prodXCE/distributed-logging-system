package com.service.logaggregator.service;

import com.service.logaggregator.model.LogEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LogProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(LogProcessingService.class);
    private final BlockingQueue<LogEvent> logQueue = new LinkedBlockingQueue<>(10000);
    private final KafkaProducerService kafkaProducerService;


    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(INFO|WARN|ERROR|DEBUG)\\s+\\[(.*?)\\\\]\\s+-\\s+(.*)", Pattern.CASE_INSENSITIVE
    );


    @Autowired
    public LogProcessingService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    public void processAndQueueLog(String rawLog, String source) {
        String level = "UNKNOWN";
        String message = rawLog;

        Matcher matcher = LOG_PATTERN.matcher(rawLog);
        if (matcher.find()) {
            level = matcher.group(1).toUpperCase();
            message = matcher.group(4);
        }

        LogEvent event = new LogEvent(
                Instant.now(),
                level,
                source,
                message,
                rawLog
        );

        boolean added = logQueue.offer(event);
        if (!added) {
            logger.warn("Log queue is full. Dropping log from source: {}", source);
        } else {
            logger.debug("Queued log from source: {}", source);
        }
    }

    @PostConstruct
    public void startLogSender() {
        Thread senderThread = new Thread(() -> {
            logger.info("Log sender thread started.");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    LogEvent logEvent = logQueue.take();
                    kafkaProducerService.sendLog(logEvent);
                } catch (InterruptedException e) {
                    logger.error("Log sender thread was interrupted.");
                    Thread.currentThread().interrupt();
                }
            }
        });

        senderThread.setName("kafka-log-sender");
        senderThread.setDaemon(true);
        senderThread.start();
    }
}
