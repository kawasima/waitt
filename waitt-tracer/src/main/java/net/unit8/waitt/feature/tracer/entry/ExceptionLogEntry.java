package net.unit8.waitt.feature.tracer.entry;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author kawasima
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExceptionLogEntry extends EntryBase {
    private final String message;
    private final StackTraceElement[] stackTrace;

    public ExceptionLogEntry(String message, StackTraceElement[] stackTrace) {
        super();
        this.message = message;
        this.stackTrace = stackTrace;
    }
}
