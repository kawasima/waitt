package net.unit8.waitt.feature.tracer.entry;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 *
 * @author kawasima
 */
@Data
public class ExceptionLogEntry implements Serializable {
    public ExceptionLogEntry(String message, StackTraceElement[] stackTrace) {
        this.message = message;
        this.stackTrace = stackTrace;
    }
    
    private Date occurredAt = new Date();
    private final String message;
    private final StackTraceElement[] stackTrace;
}
