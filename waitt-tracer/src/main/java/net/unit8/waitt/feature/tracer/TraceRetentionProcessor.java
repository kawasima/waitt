package net.unit8.waitt.feature.tracer;

import io.opentelemetry.api.common.AttributeKey;
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
 * The <em>first</em> span seen for a trace id opens the trace and binds that id to
 * the current thread (so SQL and console lines emitted on that thread correlate);
 * the trace is finalized when that same opener span ends. Keying on the opener
 * span rather than on "is root" keeps this correct when the HTTP span has an
 * ambient/incoming parent, and avoids a nested span finalizing the trace early.
 * <p>
 * Note: correlation is thread-bound, so it covers synchronous request handling;
 * work dispatched to other threads (async servlets) is not correlated.
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
        String traceId = span.getSpanContext().getTraceId();
        RequestTrace trace = store.beginTrace(traceId, System.currentTimeMillis());
        if (trace != null && trace.markOpener(span.getSpanContext().getSpanId())) {
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
        store.addSpan(traceId, toRecord(data));

        RequestTrace trace = store.getTrace(traceId);
        if (trace != null && data.getSpanId().equals(trace.getOpenerSpanId())) {
            try {
                finalizeTrace(trace, data);
            } finally {
                store.completeTrace(traceId);
                store.clearCurrentTraceId();
            }
        }
    }

    @Override
    public boolean isEndRequired() {
        return true;
    }

    private static boolean isRoot(String parentSpanId) {
        return parentSpanId == null || SpanId.getInvalid().equals(parentSpanId);
    }

    private SpanRecord toRecord(SpanData data) {
        final Map<String, String> attrs = new LinkedHashMap<String, String>();
        data.getAttributes().forEach(new java.util.function.BiConsumer<AttributeKey<?>, Object>() {
            @Override
            public void accept(AttributeKey<?> key, Object value) {
                attrs.put(key.getKey(), String.valueOf(value));
            }
        });
        boolean error = data.getStatus().getStatusCode() == StatusData.error().getStatusCode();
        return new SpanRecord(
                data.getSpanId(),
                isRoot(data.getParentSpanId()) ? null : data.getParentSpanId(),
                data.getName(),
                data.getStartEpochNanos(),
                data.getEndEpochNanos(),
                error,
                attrs);
    }

    private void finalizeTrace(RequestTrace trace, SpanData data) {
        trace.setMethod(stringAttr(data, "http.request.method"));
        trace.setPath(stringAttr(data, "url.path"));
        Long status = data.getAttributes().get(AttributeKey.longKey("http.response.status_code"));
        if (status != null) {
            trace.setStatusCode(status.intValue());
        }
        trace.setDurationMillis((data.getEndEpochNanos() - data.getStartEpochNanos()) / 1_000_000L);
        extractException(trace, data);
    }

    private void extractException(RequestTrace trace, SpanData data) {
        for (EventData event : data.getEvents()) {
            if ("exception".equals(event.getName())) {
                trace.setException(
                        eventAttr(event, "exception.type"),
                        eventAttr(event, "exception.message"),
                        eventAttr(event, "exception.stacktrace"));
                return;
            }
        }
    }

    private static String stringAttr(SpanData data, String key) {
        Object v = data.getAttributes().get(AttributeKey.stringKey(key));
        return v == null ? null : String.valueOf(v);
    }

    private static String eventAttr(EventData event, String key) {
        Object v = event.getAttributes().get(AttributeKey.stringKey(key));
        return v == null ? null : String.valueOf(v);
    }
}
