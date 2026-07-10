package net.unit8.waitt.api.observability;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Process-wide correlation hub for the dashboard's request-detail view.
 * <p>
 * Because this class lives in {@code waitt-api} — the single realm shared by the
 * webapp, tracer, and admin realms — its static singleton is genuinely one object
 * across all of them, so producers and the reader share it with plain method calls
 * (no reflection, no {@code System.getProperties()} handoff).
 * <p>
 * Spans arrive from the tracer realm (mapped from OpenTelemetry); SQL and log lines
 * are recorded from the webapp/request thread and correlated to the in-flight trace
 * via {@link #currentTraceId()} (a {@link ThreadLocal}). Completed traces are kept
 * in a bounded ring, newest first.
 *
 * @author kawasima
 */
public final class TraceStore {
    private static final TraceStore INSTANCE = new TraceStore();

    public static TraceStore getInstance() { return INSTANCE; }

    private volatile boolean enabled;
    private volatile int capacity = 100;

    private final Map<String, RequestTrace> active = new ConcurrentHashMap<String, RequestTrace>();
    private final Deque<RequestTrace> completed = new ConcurrentLinkedDeque<RequestTrace>();
    private final AtomicInteger completedCount = new AtomicInteger(0);
    private final ThreadLocal<String> currentTraceId = new ThreadLocal<String>();
    private volatile TraceListener listener;

    private TraceStore() {}

    /** Notified when a trace completes, so the dashboard can push it live. */
    public interface TraceListener {
        void onTraceCompleted(RequestTrace trace);
    }

    public void setListener(TraceListener listener) { this.listener = listener; }

    /** Enabled by the admin feature when the dashboard is present; off by default. */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isEnabled() { return enabled; }

    public void setCapacity(int capacity) { if (capacity > 0) this.capacity = capacity; }

    // --- trace assembly (driven by the tracer's span processor) ---

    /** Start (or fetch) the in-flight trace for a root span. */
    public RequestTrace beginTrace(String traceId, long startEpochMillis) {
        if (!enabled || traceId == null) {
            return null;
        }
        RequestTrace trace = active.get(traceId);
        if (trace == null) {
            trace = new RequestTrace(traceId, startEpochMillis);
            RequestTrace existing = active.putIfAbsent(traceId, trace);
            if (existing != null) {
                trace = existing;
            }
        }
        return trace;
    }

    public void addSpan(String traceId, SpanRecord span) {
        if (!enabled || traceId == null) {
            return;
        }
        RequestTrace trace = active.get(traceId);
        if (trace != null) {
            trace.addSpan(span);
        }
    }

    /** Finalize the root span's trace and move it into the completed ring. */
    public void completeTrace(String traceId) {
        if (traceId == null) {
            return;
        }
        RequestTrace trace = active.remove(traceId);
        if (trace == null) {
            return;
        }
        completed.addFirst(trace);
        completedCount.incrementAndGet();
        // O(1) trim: ConcurrentLinkedDeque.size() is O(n), so track the count.
        while (completedCount.get() > capacity) {
            if (completed.pollLast() == null) {
                break;
            }
            completedCount.decrementAndGet();
        }
        TraceListener l = listener;
        if (l != null) {
            try {
                l.onTraceCompleted(trace);
            } catch (RuntimeException ignored) {
                // a listener error must not break span processing
            }
        }
    }

    // --- request-thread correlation (SQL, logs) ---

    public void setCurrentTraceId(String traceId) { currentTraceId.set(traceId); }
    public void clearCurrentTraceId() { currentTraceId.remove(); }
    public String currentTraceId() { return currentTraceId.get(); }

    public void recordSql(SqlEvent event) {
        RequestTrace trace = currentActiveTrace();
        if (trace != null) {
            trace.addSqlEvent(event);
        }
    }

    public void recordLog(LogLine line) {
        RequestTrace trace = currentActiveTrace();
        if (trace != null) {
            trace.addLog(line);
        }
    }

    private RequestTrace currentActiveTrace() {
        if (!enabled) {
            return null;
        }
        String traceId = currentTraceId.get();
        return traceId == null ? null : active.get(traceId);
    }

    // --- read side (admin UI) ---

    public List<RequestTrace> recentTraces() {
        return new ArrayList<RequestTrace>(completed);
    }

    public RequestTrace getTrace(String traceId) {
        if (traceId == null) {
            return null;
        }
        RequestTrace trace = active.get(traceId);
        if (trace != null) {
            return trace;
        }
        for (RequestTrace t : completed) {
            if (traceId.equals(t.getTraceId())) {
                return t;
            }
        }
        return null;
    }

    public int size() { return completedCount.get(); }

    /** Drop all retained state (used on shutdown and by tests). */
    public void clear() {
        active.clear();
        completed.clear();
        completedCount.set(0);
        currentTraceId.remove();
    }
}
