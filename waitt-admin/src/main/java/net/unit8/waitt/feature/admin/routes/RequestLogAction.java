package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.ResponseUtils;
import net.unit8.waitt.feature.admin.Route;
import net.unit8.waitt.feature.admin.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stores and serves recent HTTP request logs.
 * Requests are recorded via {@link net.unit8.waitt.api.EmbeddedServer.RequestListener}
 * set up in AdminServer, using server-level Valve (Tomcat) or Handler (Jetty).
 *
 * @author kawasima
 */
public class RequestLogAction implements Route {
    private static final int MAX_ENTRIES = 200;
    static final String LOG_KEY = "waitt.request.log";
    static final String COUNT_KEY = "waitt.request.log.count";
    private final Deque<Map<String, Object>> logEntries;
    private final AtomicInteger count;

    @SuppressWarnings("unchecked")
    public RequestLogAction() {
        Object existing = System.getProperties().get(LOG_KEY);
        if (existing instanceof Deque) {
            logEntries = (Deque<Map<String, Object>>) existing;
        } else {
            logEntries = new ConcurrentLinkedDeque<Map<String, Object>>();
            System.getProperties().put(LOG_KEY, logEntries);
        }
        Object existingCount = System.getProperties().get(COUNT_KEY);
        if (existingCount instanceof AtomicInteger) {
            count = (AtomicInteger) existingCount;
        } else {
            count = new AtomicInteger(0);
            System.getProperties().put(COUNT_KEY, count);
        }
    }

    public static void record(String method, String path, int status, long durationMs) {
        @SuppressWarnings("unchecked")
        Deque<Map<String, Object>> log = (Deque<Map<String, Object>>) System.getProperties().get(LOG_KEY);
        if (log == null) return;

        AtomicInteger cnt = (AtomicInteger) System.getProperties().get(COUNT_KEY);

        Map<String, Object> entry = new LinkedHashMap<String, Object>();
        entry.put("timestamp", System.currentTimeMillis());
        entry.put("method", method);
        entry.put("path", path);
        entry.put("status", status);
        entry.put("duration", durationMs);
        log.addFirst(entry);
        int size = cnt != null ? cnt.incrementAndGet() : log.size();
        while (size > MAX_ENTRIES) {
            if (log.pollLast() != null) {
                size = cnt != null ? cnt.decrementAndGet() : log.size();
            } else {
                break;
            }
        }
    }

    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod())
                && "/requests".equals(exchange.getRequestURI().getPath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject json = new JSONObject();
        List<JSONObject> entries = new ArrayList<JSONObject>();
        for (Map<String, Object> entry : logEntries) {
            JSONObject e = new JSONObject();
            for (Map.Entry<String, Object> kv : entry.entrySet()) {
                e.put(kv.getKey(), kv.getValue());
            }
            entries.add(e);
        }
        json.put("requests", entries);
        json.put("total", count.get());
        ResponseUtils.responseJSON(exchange, json);
    }
}
