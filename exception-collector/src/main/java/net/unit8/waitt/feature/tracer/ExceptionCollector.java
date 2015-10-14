package net.unit8.waitt.feature.tracer;

import java.util.Map;
import net.unit8.waitt.api.ConfigurableFeature;
import net.unit8.waitt.api.LogListener;
import net.unit8.waitt.api.configuration.Feature;
import net.unit8.waitt.api.configuration.WebappConfiguration;

/**
 * @author kawasima
 */
public class ExceptionCollector implements LogListener, ConfigurableFeature {
    private ESClient esclient;
    
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
        esclient = new ESClient(baseUrl);
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
            esclient.post("/waitt/exception/", new ExceptionLogEntry(message.toString(), t.getStackTrace()));
        }
    }
}
