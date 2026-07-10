package net.unit8.waitt.api.observability;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single span mapped from an OpenTelemetry span into a plain, ClassRealm-neutral
 * form. Lives in {@code waitt-api} so the tracer realm (which produces it) and the
 * admin realm (which renders it) share the same type without reflection.
 *
 * @author kawasima
 */
public final class SpanRecord {
    private final String spanId;
    private final String parentSpanId;
    private final String name;
    private final long startEpochNanos;
    private final long endEpochNanos;
    private final boolean error;
    private final Map<String, String> attributes;

    public SpanRecord(String spanId, String parentSpanId, String name,
                      long startEpochNanos, long endEpochNanos, boolean error,
                      Map<String, String> attributes) {
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
        this.name = name;
        this.startEpochNanos = startEpochNanos;
        this.endEpochNanos = endEpochNanos;
        this.error = error;
        this.attributes = attributes == null
                ? Collections.<String, String>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<String, String>(attributes));
    }

    public String getSpanId() { return spanId; }
    public String getParentSpanId() { return parentSpanId; }
    public String getName() { return name; }
    public long getStartEpochNanos() { return startEpochNanos; }
    public long getEndEpochNanos() { return endEpochNanos; }
    public long getDurationNanos() { return endEpochNanos - startEpochNanos; }
    public boolean isError() { return error; }
    public Map<String, String> getAttributes() { return attributes; }

    /** True when this span carries database attributes (a SQL/JDBC span). */
    public boolean isDatabase() {
        return attributes.containsKey("db.statement") || attributes.containsKey("db.system");
    }
}
