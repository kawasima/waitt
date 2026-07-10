package net.unit8.waitt.feature.tracer;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import net.unit8.waitt.api.ConfigurableFeature;
import net.unit8.waitt.api.EmbeddedServer;
import net.unit8.waitt.api.ServerMonitor;
import net.unit8.waitt.api.configuration.Feature;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import net.unit8.waitt.api.observability.TraceStore;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the OpenTelemetry SDK lifecycle.
 * Shares the Tracer instance via System properties to bridge ClassLoader boundaries.
 *
 * @author kawasima
 */
public class TracerLifecycle implements ServerMonitor, ConfigurableFeature {
    private static final Logger LOG = Logger.getLogger(TracerLifecycle.class.getName());
    private static final String DEFAULT_ENDPOINT = "http://localhost:4318";
    private static final String TRACER_PROPERTY_KEY = "waitt.otel.tracer";

    private OpenTelemetrySdk sdk;
    private String endpoint = DEFAULT_ENDPOINT;
    private String serviceName = "waitt-app";
    private int retentionSize = 100;

    @Override
    public void config(WebappConfiguration config) {
        if (config.getApplicationName() != null) {
            serviceName = config.getApplicationName();
        }
        for (Feature feature : config.getFeatures()) {
            if ("waitt-tracer".equals(feature.getArtifactId())
                    && "net.unit8.waitt.feature".equals(feature.getGroupId())) {
                Map<String, String> featureConfig = feature.getConfiguration();
                if (featureConfig != null) {
                    if (featureConfig.containsKey("otel.endpoint")) {
                        endpoint = featureConfig.get("otel.endpoint");
                    }
                    if (featureConfig.containsKey("otel.service.name")) {
                        serviceName = featureConfig.get("otel.service.name");
                    }
                    if (featureConfig.containsKey("trace.retention.size")) {
                        try {
                            retentionSize = Integer.parseInt(featureConfig.get("trace.retention.size"));
                        } catch (NumberFormatException e) {
                            LOG.warning("Invalid trace.retention.size: " + featureConfig.get("trace.retention.size"));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void init(EmbeddedServer server) {
        try {
            Resource resource = Resource.getDefault().merge(
                    Resource.create(Attributes.of(
                            AttributeKey.stringKey("service.name"), serviceName
                    ))
            );

            String tracesEndpoint = endpoint.contains("/v1/traces") ? endpoint
                    : endpoint.endsWith("/") ? endpoint + "v1/traces"
                    : endpoint + "/v1/traces";
            OtlpHttpSpanExporter spanExporter = OtlpHttpSpanExporter.builder()
                    .setEndpoint(tracesEndpoint)
                    .build();

            TraceStore store = TraceStore.getInstance();
            store.setCapacity(retentionSize);
            store.setEnabled(true);

            SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                    .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                    .addSpanProcessor(new TraceRetentionProcessor(store))
                    .setResource(resource)
                    .build();

            sdk = OpenTelemetrySdk.builder()
                    .setTracerProvider(tracerProvider)
                    .build();

            Tracer tracer = sdk.getTracer("waitt-tracer");
            System.getProperties().put(TRACER_PROPERTY_KEY, tracer);
            LOG.info("OpenTelemetry tracer initialized (endpoint=" + endpoint + ", service=" + serviceName + ")");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to initialize OpenTelemetry tracer", e);
        }
    }

    @Override
    public void start(EmbeddedServer server) {
        // no-op
    }

    @Override
    public void stop() {
        System.getProperties().remove(TRACER_PROPERTY_KEY);
        TraceStore.getInstance().setEnabled(false);
        if (sdk != null) {
            sdk.close();
            LOG.info("OpenTelemetry tracer shut down.");
        }
    }
}
