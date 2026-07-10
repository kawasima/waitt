package net.unit8.waitt.feature.tracer;

import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import net.unit8.waitt.api.observability.RequestTrace;
import net.unit8.waitt.api.observability.SpanRecord;
import net.unit8.waitt.api.observability.TraceStore;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Retains OpenTelemetry spans in the shared {@link TraceStore} for the dashboard's
 * request-detail view, mapping each span into the ClassRealm-neutral
 * {@link SpanRecord}. Runs in the tracer realm (which owns the OTel classes) and
 * writes only {@code waitt-api} types, so the admin realm reads them with no
 * reflection.
 * <p>
 * On a root span's start it opens the request's trace and binds the trace id to
 * the current thread (via {@link TraceStore#setCurrentTraceId(String)}) so SQL and
 * console lines emitted on that thread correlate; on the root span's end it
 * finalizes the trace and clears the thread binding.
 *
 * @author kawasima
 */
public class TraceRetentionProcessor implements SpanProcessor {
    private final TraceStore store;

    public TraceRetentionProcessor(TraceStore store) {
        this.store = store;
    }

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        if (isRoot(span.getParentSpanContext().getSpanId())) {
            String traceId = span.getSpanContext().getTraceId();
            store.beginTrace(traceId, System.currentTimeMillis());
            store.setCurrentTraceId(traceId);
        }
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {
        SpanData data = span.toSpanData();
        String traceId = data.getTraceId();
        boolean root = isRoot(data.getParentSpanId());

        store.addSpan(traceId, toRecord(data, root));

        if (root) {
            finalizeTrace(traceId, data);
            store.completeTrace(traceId);
            store.clearCurrentTraceId();
        }
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }

    private static boolean isRoot(String parentSpanId) {
        return parentSpanId == null || SpanId.getInvalid().equals(parentSpanId);
    }

    private SpanRecord toRecord(SpanData data, boolean root) {
        final Map<String, String> attrs = new LinkedHashMap<String, String>();
        data.getAttributes().forEach(new java.util.function.BiConsumer<io.opentelemetry.api.common.AttributeKey<?>, Object>() {
            @Override
            public void accept(io.opentelemetry.api.common.AttributeKey<?> key, Object value) {
                attrs.put(key.getKey(), String.valueOf(value));
            }
        });
        boolean error = data.getStatus().getStatusCode() == StatusData.error().getStatusCode();
        return new SpanRecord(
                data.getSpanId(),
                root ? null : data.getParentSpanId(),
                data.getName(),
                data.getStartEpochNanos(),
                data.getEndEpochNanos(),
                error,
                attrs);
    }

    private void finalizeTrace(String traceId, SpanData data) {
        RequestTrace trace = store.getTrace(traceId);
        if (trace == null) {
            return;
        }
        trace.setMethod(attr(data, "http.request.method"));
        trace.setPath(attr(data, "url.path"));
        String status = attr(data, "http.response.status_code");
        if (status != null) {
            try {
                trace.setStatusCode((int) Double.parseDouble(status));
            } catch (NumberFormatException ignored) {
                // leave status unset
            }
        }
        trace.setDurationMillis((data.getEndEpochNanos() - data.getStartEpochNanos()) / 1_000_000L);
        extractException(trace, data);
    }

    private void extractException(RequestTrace trace, SpanData data) {
        for (EventData event : data.getEvents()) {
            if ("exception".equals(event.getName())) {
                trace.setException(
                        stringAttr(event, "exception.type"),
                        stringAttr(event, "exception.message"),
                        stringAttr(event, "exception.stacktrace"));
                return;
            }
        }
    }

    private static String attr(SpanData data, String key) {
        Object v = data.getAttributes().get(io.opentelemetry.api.common.AttributeKey.stringKey(key));
        if (v != null) {
            return String.valueOf(v);
        }
        // status_code is a long attribute; fall back to a generic scan.
        final String[] found = new String[1];
        data.getAttributes().forEach(new java.util.function.BiConsumer<io.opentelemetry.api.common.AttributeKey<?>, Object>() {
            @Override
            public void accept(io.opentelemetry.api.common.AttributeKey<?> k, Object value) {
                if (k.getKey().equals(key)) {
                    found[0] = String.valueOf(value);
                }
            }
        });
        return found[0];
    }

    private static String stringAttr(EventData event, String key) {
        final String[] found = new String[1];
        event.getAttributes().forEach(new java.util.function.BiConsumer<io.opentelemetry.api.common.AttributeKey<?>, Object>() {
            @Override
            public void accept(io.opentelemetry.api.common.AttributeKey<?> k, Object value) {
                if (k.getKey().equals(key)) {
                    found[0] = String.valueOf(value);
                }
            }
        });
        return found[0];
    }
}
