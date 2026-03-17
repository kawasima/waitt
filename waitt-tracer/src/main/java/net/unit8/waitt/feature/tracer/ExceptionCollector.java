package net.unit8.waitt.feature.tracer;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import net.unit8.waitt.api.LogListener;

/**
 * Collects application exceptions and records them as OpenTelemetry spans.
 *
 * @author kawasima
 */
public class ExceptionCollector implements LogListener {

    @Override
    public void info(CharSequence message, Throwable t) {
    }

    @Override
    public void debug(CharSequence message, Throwable t) {
    }

    @Override
    public void warn(CharSequence message, Throwable t) {
    }

    @Override
    public void error(CharSequence message, Throwable t) {
        if (t == null) {
            return;
        }
        Tracer tracer = TracerLifecycle.getTracer();
        if (tracer == null) {
            return;
        }
        Span span = tracer.spanBuilder("exception")
                .setAttribute("exception.message", message.toString())
                .startSpan();
        try {
            span.setStatus(StatusCode.ERROR, message.toString());
            span.recordException(t);
        } finally {
            span.end();
        }
    }
}
