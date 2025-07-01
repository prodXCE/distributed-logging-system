package com.service.logaggregator.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

/**
 * structure for log event.
 * standard format.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEvent {

     // timestamp for log gen
     private Instant timestamp;

     // severity level of log (INFO, ERROR, WARN..)
     private String level;

     // source of log
     private String source;

     // core, readable msgs extracted from logs
     private String message;

     // complete raw log string
     private String rawMessage;
}
