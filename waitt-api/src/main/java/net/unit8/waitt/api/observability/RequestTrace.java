package net.unit8.waitt.api.observability;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

    private final List<SpanRecord> spans = new CopyOnWriteArrayList<SpanRecord>();
    private final List<SqlEvent> sqlEvents = new CopyOnWriteArrayList<SqlEvent>();
    private final List<LogLine> logs = new CopyOnWriteArrayList<LogLine>();

    public RequestTrace(String traceId, long startEpochMillis) {
        this.traceId = traceId;
        this.startEpochMillis = startEpochMillis;
    }

    public String getTraceId() { return traceId; }
    public long getStartEpochMillis() { return startEpochMillis; }

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

    public void addSpan(SpanRecord span) { spans.add(span); }
    public void addSqlEvent(SqlEvent event) { sqlEvents.add(event); }
    public void addLog(LogLine line) { logs.add(line); }

    public List<SpanRecord> getSpans() { return spans; }
    public List<SqlEvent> getSqlEvents() { return sqlEvents; }
    public List<LogLine> getLogs() { return logs; }
}
