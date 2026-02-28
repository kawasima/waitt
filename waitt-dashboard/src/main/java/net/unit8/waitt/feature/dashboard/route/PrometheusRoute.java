package net.unit8.waitt.feature.dashboard.route;

import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.unit8.waitt.feature.dashboard.Route;

public class PrometheusRoute implements Route {
    PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    private final JvmGcMetrics jvmGcMetrics;

    public PrometheusRoute() {
        this.jvmGcMetrics = new JvmGcMetrics();
        jvmGcMetrics.bindTo(prometheusRegistry);
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);
    }

    @Override
    public Object handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/plain;version=0.0.4;charset=utf-8");
        return prometheusRegistry.scrape();
    }
}
