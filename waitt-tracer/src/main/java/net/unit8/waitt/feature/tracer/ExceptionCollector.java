package net.unit8.waitt.feature.tracer;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import net.unit8.waitt.api.LogListener;

/**
 * Collects application exceptions and records them as OpenTelemetry spans.
 * Runs in the same ClassRealm as TracerLifecycle, so direct API access works.
 *
 * @author kawasima
 */
public class ExceptionCollector implements LogListener {

    private Tracer getTracer() {
        Object obj = System.getProperties().get("waitt.otel.tracer");
        return (obj instanceof Tracer) ? (Tracer) obj : null;
    }

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
        Tracer tracer = getTracer();
        if (tracer == null) {
            return;
        }
        String msg = message != null ? message.toString()
                : t.getMessage() != null ? t.getMessage() : t.toString();
        Span span = tracer.spanBuilder("exception")
                .setAttribute("exception.message", msg)
                .startSpan();
        try {
            span.setStatus(StatusCode.ERROR, msg);
            span.recordException(t);
        } finally {
            span.end();
        }
    }
}
