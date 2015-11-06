package net.unit8.waitt.feature.tracer;

import java.util.Map;
import java.util.logging.Logger;

import net.unit8.waitt.api.ConfigurableFeature;
import net.unit8.waitt.api.LogListener;
import net.unit8.waitt.api.configuration.Feature;
import net.unit8.waitt.api.configuration.WebappConfiguration;
import net.unit8.waitt.feature.tracer.entry.ExceptionLogEntry;

/**
 * @author kawasima
 */
public class ExceptionCollector implements LogListener, ConfigurableFeature {
    private static final Logger LOG = Logger.getLogger(ExceptionCollector.class.getName());
    private ESClient esClient;

    @Override
    public void config(WebappConfiguration config) {
        String baseUrl = null;
        for (Feature feature : config.getFeatures()) {
            if ("waitt-tracer".equals(feature.getArtifactId()) && "net.unit8.waitt.feature".equals(feature.getGroupId())) {
                Map<String, String> featureConfig = feature.getConfiguration();
                if (featureConfig != null) {
                    baseUrl = featureConfig.get("elasticsearch.url");
                }
            }
        }
        if (baseUrl == null) {
            baseUrl = "http://localhost:9200";
        }
        esClient = new ESClient(baseUrl);
        LOG.info("setup ESClient for " + baseUrl);
    }
    
    @Override
    public void info(CharSequence message, Throwable t) {
    }

    @Override
    public void debug(CharSequence message, Throwable t) {
    }

    @Override
    public void warn(CharSequence message, Throwable t) {
    }

    @Override
    public void error(CharSequence message, Throwable t) {
        System.out.println("log:error:" + message + ":"+ t);

        if (t != null) {
            esClient.post("/waitt/exception/", new ExceptionLogEntry(message.toString(), t.getStackTrace()));
        }
    }
}
