package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.LogBuffer;
import net.unit8.waitt.feature.admin.LogEntry;
import net.unit8.waitt.feature.admin.ResponseUtils;
import net.unit8.waitt.feature.admin.Route;
import net.unit8.waitt.feature.admin.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serves a snapshot of the captured console lines. Live updates arrive over
 * {@code /stream} as {@code log} events; this endpoint seeds the initial view.
 *
 * @author kawasima
 */
public class LogsAction implements Route {
    private final LogBuffer buffer;

    public LogsAction(LogBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod())
                && "/logs".equals(exchange.getRequestURI().getPath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        List<LogEntry> entries = buffer.snapshot();
        List<JSONObject> logs = new ArrayList<JSONObject>(entries.size());
        for (LogEntry entry : entries) {
            logs.add(entry.toJSON());
        }
        JSONObject json = new JSONObject();
        json.put("logs", logs);
        json.put("total", entries.size());
        ResponseUtils.responseJSON(exchange, json);
    }
}
