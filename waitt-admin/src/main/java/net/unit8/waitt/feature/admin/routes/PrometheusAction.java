package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import net.unit8.waitt.feature.admin.AdminMetrics;
import net.unit8.waitt.feature.admin.ResponseUtils;
import net.unit8.waitt.feature.admin.Route;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

/**
 * Expose Prometheus metrics from the shared {@link AdminMetrics} registry.
 *
 * @author kawasima
 */
public class PrometheusAction implements Route {
    private final AdminMetrics metrics;

    public PrometheusAction(AdminMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod())
                && "/prometheus".equals(exchange.getRequestURI().getPath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        byte[] body = metrics.scrape().getBytes("UTF-8");
        exchange.getResponseHeaders().put("Content-Type",
                Collections.singletonList("text/plain; version=0.0.4; charset=utf-8"));
        ResponseUtils.applyCorsOrigin(exchange);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}
