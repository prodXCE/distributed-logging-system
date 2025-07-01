package com.service.logaggregator.service;

import com.service.logaggregator.model.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "logs-topic";

    private final KafkaTemplate<String, LogEvent> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, LogEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // sends a LogEvent to the Kafka topic.
    public void sendLog(LogEvent logEvent) {
        try {
            kafkaTemplate.send(TOPIC, logEvent).whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Sent log to Kafka topic '{}': key={}, partition={}, offset={}",
                            TOPIC, logEvent.getSource(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to send log to Kafka topic '{}'", TOPIC, ex);
                }
            });
        } catch (Exception e) {
                logger.error("Exception while sending log to Kafka", e);
        }
    }
}


