package net.unit8.waitt.api.observability;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything captured for one HTTP request, keyed by its OpenTelemetry trace id:
 * the spans (HTTP + any nested), the SQL statements it ran, the console lines it
 * emitted, and the exception that escaped it. Assembled in {@link TraceStore} and
 * rendered by the admin dashboard as a correlated request-detail view.
 *
 * @author kawasima
 */
public final class RequestTrace {
    private final String traceId;
    private final long startEpochMillis;

    private volatile String method;
    private volatile String path;
    private volatile int statusCode;
    private volatile long durationMillis = -1;

    private volatile String exceptionType;
    private volatile String exceptionMessage;
    private volatile String exceptionStack;
    private volatile String openerSpanId;

    // Single-writer high-frequency appends (all from the request thread); readers
    // (the admin thread, at request end) get a synchronized snapshot. A plain
    // synchronized ArrayList beats CopyOnWriteArrayList here, whose per-add full
    // copy would be O(n^2) for a request that logs many lines.
    private final List<SpanRecord> spans = new ArrayList<SpanRecord>();
    private final List<SqlEvent> sqlEvents = new ArrayList<SqlEvent>();
    private final List<LogLine> logs = new ArrayList<LogLine>();

    public RequestTrace(String traceId, long startEpochMillis) {
        this.traceId = traceId;
        this.startEpochMillis = startEpochMillis;
    }

    public String getTraceId() { return traceId; }
    public long getStartEpochMillis() { return startEpochMillis; }

    /**
     * The span that opened this trace (the first span seen for the trace id).
     * The trace is finalized when this span ends, which is robust against a
     * non-root HTTP span (ambient parent) and against nested spans.
     */
    public String getOpenerSpanId() { return openerSpanId; }

    /** Claim this trace's opener slot; true only for the first caller. */
    public synchronized boolean markOpener(String spanId) {
        if (openerSpanId == null) {
            openerSpanId = spanId;
            return true;
        }
        return false;
    }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public long getDurationMillis() { return durationMillis; }
    public void setDurationMillis(long durationMillis) { this.durationMillis = durationMillis; }

    public String getExceptionType() { return exceptionType; }
    public String getExceptionMessage() { return exceptionMessage; }
    public String getExceptionStack() { return exceptionStack; }
    public void setException(String type, String message, String stack) {
        this.exceptionType = type;
        this.exceptionMessage = message;
        this.exceptionStack = stack;
    }

    public void addSpan(SpanRecord span) { synchronized (spans) { spans.add(span); } }
    public void addSqlEvent(SqlEvent event) { synchronized (sqlEvents) { sqlEvents.add(event); } }
    public void addLog(LogLine line) { synchronized (logs) { logs.add(line); } }

    public List<SpanRecord> getSpans() { synchronized (spans) { return new ArrayList<SpanRecord>(spans); } }
    public List<SqlEvent> getSqlEvents() { synchronized (sqlEvents) { return new ArrayList<SqlEvent>(sqlEvents); } }
    public List<LogLine> getLogs() { synchronized (logs) { return new ArrayList<LogLine>(logs); } }
}
