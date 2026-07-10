package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.api.observability.LogLine;
import net.unit8.waitt.api.observability.RequestTrace;
import net.unit8.waitt.api.observability.SpanRecord;
import net.unit8.waitt.api.observability.SqlEvent;
import net.unit8.waitt.api.observability.TraceStore;
import net.unit8.waitt.feature.admin.ResponseUtils;
import net.unit8.waitt.feature.admin.Route;
import net.unit8.waitt.feature.admin.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Serves the correlated request traces from {@link TraceStore}:
 * {@code GET /traces} lists recent traces (summaries), {@code GET /traces/{id}}
 * returns one trace's full detail (spans, SQL, logs, exception). Live updates
 * arrive over {@code /stream} as {@code trace} events.
 *
 * @author kawasima
 */
public class TracesAction implements Route {
    private static final String PREFIX = "/traces";

    @Override
    public boolean canHandle(HttpExchange exchange) {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            return false;
        }
        String path = exchange.getRequestURI().getPath();
        return PREFIX.equals(path) || path.startsWith(PREFIX + "/");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (PREFIX.equals(path)) {
            handleList(exchange);
        } else {
            handleDetail(exchange, path.substring(PREFIX.length() + 1));
        }
    }

    private void handleList(HttpExchange exchange) throws IOException {
        List<RequestTrace> traces = TraceStore.getInstance().recentTraces();
        List<JSONObject> list = new ArrayList<JSONObject>(traces.size());
        for (RequestTrace trace : traces) {
            list.add(summary(trace));
        }
        JSONObject json = new JSONObject();
        json.put("traces", list);
        json.put("total", traces.size());
        ResponseUtils.responseJSON(exchange, json);
    }

    private void handleDetail(HttpExchange exchange, String traceId) throws IOException {
        RequestTrace trace = TraceStore.getInstance().getTrace(traceId);
        if (trace == null) {
            ResponseUtils.sendError(exchange, 404, "Trace not found");
            return;
        }
        ResponseUtils.responseJSON(exchange, detail(trace));
    }

    public static JSONObject summary(RequestTrace trace) {
        JSONObject o = new JSONObject();
        o.put("traceId", trace.getTraceId());
        o.put("method", trace.getMethod());
        o.put("path", trace.getPath());
        o.put("statusCode", trace.getStatusCode());
        o.put("startEpochMillis", trace.getStartEpochMillis());
        o.put("durationMillis", trace.getDurationMillis());
        o.put("spanCount", trace.getSpans().size());
        o.put("sqlCount", trace.getSqlEvents().size());
        o.put("logCount", trace.getLogs().size());
        o.put("hasError", trace.getExceptionType() != null || trace.getStatusCode() >= 500);
        return o;
    }

    private JSONObject detail(RequestTrace trace) {
        JSONObject o = summary(trace);

        // Absolute epoch nanos (~1.8e18) exceed JS's safe-integer range, so emit
        // offsets relative to the earliest span start (small longs the browser can
        // position exactly) plus the total window for the waterfall.
        long minStart = Long.MAX_VALUE;
        long maxEnd = Long.MIN_VALUE;
        for (SpanRecord span : trace.getSpans()) {
            minStart = Math.min(minStart, span.getStartEpochNanos());
            maxEnd = Math.max(maxEnd, span.getEndEpochNanos());
        }
        long windowNanos = trace.getSpans().isEmpty() ? 0 : (maxEnd - minStart);
        o.put("windowNanos", windowNanos);

        List<JSONObject> spans = new ArrayList<JSONObject>();
        for (SpanRecord span : trace.getSpans()) {
            JSONObject s = new JSONObject();
            s.put("spanId", span.getSpanId());
            s.put("parentSpanId", span.getParentSpanId());
            s.put("name", span.getName());
            s.put("startOffsetNanos", span.getStartEpochNanos() - minStart);
            s.put("durationNanos", span.getDurationNanos());
            s.put("error", span.isError());
            s.put("database", span.isDatabase());
            JSONObject attrs = new JSONObject();
            for (Map.Entry<String, String> e : span.getAttributes().entrySet()) {
                attrs.put(e.getKey(), e.getValue());
            }
            s.put("attributes", attrs);
            spans.add(s);
        }
        o.put("spans", spans);

        List<JSONObject> sqls = new ArrayList<JSONObject>();
        for (SqlEvent sql : trace.getSqlEvents()) {
            JSONObject q = new JSONObject();
            q.put("startEpochMillis", sql.getStartEpochMillis());
            q.put("durationMillis", sql.getDurationMillis());
            q.put("sql", sql.getSql());
            q.put("rowCount", sql.getRowCount());
            q.put("success", sql.isSuccess());
            q.put("error", sql.getError());
            sqls.add(q);
        }
        o.put("sqlEvents", sqls);

        List<JSONObject> logs = new ArrayList<JSONObject>();
        for (LogLine line : trace.getLogs()) {
            JSONObject l = new JSONObject();
            l.put("timestamp", line.getTimestampMillis());
            l.put("stream", line.getStream());
            l.put("text", line.getText());
            logs.add(l);
        }
        o.put("logs", logs);

        if (trace.getExceptionType() != null) {
            JSONObject ex = new JSONObject();
            ex.put("type", trace.getExceptionType());
            ex.put("message", trace.getExceptionMessage());
            ex.put("stack", trace.getExceptionStack());
            o.put("exception", ex);
        }
        return o;
    }
}
