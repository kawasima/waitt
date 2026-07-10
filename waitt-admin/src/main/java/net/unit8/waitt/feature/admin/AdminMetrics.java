package net.unit8.waitt.feature.admin;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

/**
 * Owns the shared Micrometer registry for the admin server.
 * <p>
 * JVM metrics (GC/memory/thread) are bound once, and HTTP request metrics are
 * recorded per request through {@link #recordHttpRequest(String, int, long)}.
 * Both the Prometheus scrape endpoint and the request listener use this single
 * registry, so a scrape reflects live traffic.
 *
 * @author kawasima
 */
public class AdminMetrics implements Closeable {
    private final PrometheusMeterRegistry registry;
    private final JvmGcMetrics gcMetrics;

    public AdminMetrics() {
        registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        gcMetrics = new JvmGcMetrics();
        gcMetrics.bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
    }

    /**
     * Record one HTTP request. The path is intentionally not used as a tag to
     * avoid unbounded cardinality over a long-running development session.
     */
    public void recordHttpRequest(String method, int status, long durationMs) {
        Timer.builder("http.server.requests")
                .tag("method", method == null ? "UNKNOWN" : method)
                .tag("status", Integer.toString(status))
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public String scrape() {
        return registry.scrape();
    }

    @Override
    public void close() {
        gcMetrics.close();
        registry.close();
    }
}
