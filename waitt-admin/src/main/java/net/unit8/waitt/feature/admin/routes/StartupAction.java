package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.ResponseUtils;
import net.unit8.waitt.feature.admin.Route;
import net.unit8.waitt.feature.admin.json.JSONObject;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Show startup timeline (feature init/start durations).
 *
 * @author kawasima
 */
public class StartupAction implements Route {
    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod())
                && "/startup".equals(exchange.getRequestURI().getPath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        JSONObject json = new JSONObject();
        json.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());

        @SuppressWarnings("unchecked")
        List<String[]> timeline = (List<String[]>) System.getProperties().get("waitt.startup.timeline");
        List<JSONObject> entries = new ArrayList<JSONObject>();
        if (timeline != null) {
            for (String[] entry : timeline) {
                JSONObject e = new JSONObject();
                e.put("component", entry[0]);
                e.put("phase", entry[1]);
                e.put("duration", Long.parseLong(entry[2]));
                entries.add(e);
            }
        }
        json.put("timeline", entries);
        ResponseUtils.responseJSON(exchange, json);
    }
}
