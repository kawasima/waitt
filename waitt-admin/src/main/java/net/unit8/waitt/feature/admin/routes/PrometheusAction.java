package net.unit8.waitt.feature.admin.routes;

import com.sun.net.httpserver.HttpExchange;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import net.unit8.waitt.feature.admin.Route;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

/**
 * Expose Prometheus metrics.
 *
 * @author kawasima
 */
public class PrometheusAction implements Route {
    private final PrometheusMeterRegistry registry;

    public PrometheusAction() {
        registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        new JvmGcMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
    }

    @Override
    public boolean canHandle(HttpExchange exchange) {
        return "GET".equalsIgnoreCase(exchange.getRequestMethod())
                && "/prometheus".equals(exchange.getRequestURI().getPath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        byte[] body = registry.scrape().getBytes("UTF-8");
        exchange.getResponseHeaders().put("Content-Type",
                Collections.singletonList("text/plain; version=0.0.4; charset=utf-8"));
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}
