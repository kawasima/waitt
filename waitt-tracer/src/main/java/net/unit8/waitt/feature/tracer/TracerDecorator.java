package net.unit8.waitt.feature.tracer;

import net.unit8.waitt.api.WebappDecorator;
import net.unit8.waitt.api.configuration.FilterConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registers the OTel tracing filter as a WebappDecorator.
 *
 * @author kawasima
 */
public class TracerDecorator implements WebappDecorator {
    @Override
    public List<FilterConfiguration> getFilterConfigs() {
        List<FilterConfiguration> filterConfigurations = new ArrayList<FilterConfiguration>();
        FilterConfiguration config = new FilterConfiguration();
        config.setName("otelTracingFilter");
        config.setClassName("net.unit8.waitt.feature.tracer.ResponseDumpFilter");
        config.setUrlPatterns(Collections.singletonList("/*"));
        filterConfigurations.add(config);
        return filterConfigurations;
    }
}
