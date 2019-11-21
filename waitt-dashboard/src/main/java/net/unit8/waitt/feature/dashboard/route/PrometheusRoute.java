package net.unit8.waitt.feature.dashboard.route;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import spark.Request;
import spark.Response;
import spark.Route;

public class PrometheusRoute implements Route {
    PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    public PrometheusRoute() {
        new JvmGcMetrics().bindTo(prometheusRegistry);
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("text/plain;version=0.0.4;charset=utf-8");
        return prometheusRegistry.scrape();
    }
}
