package net.unit8.waitt.feature.tracer;

import net.unit8.waitt.api.WebappDecorator;
import net.unit8.waitt.api.configuration.FilterConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kawasima
 */
public class TracerDecorator implements WebappDecorator {
    @Override
    public List<FilterConfiguration> getFilterConfigs() {
        List<FilterConfiguration> filterConfigurations = new ArrayList<FilterConfiguration>();
        FilterConfiguration responseDumpFilterConfig = new FilterConfiguration();
        responseDumpFilterConfig.setName("responseDumpConfig");
        responseDumpFilterConfig.setClassName("net.unit8.waitt.feature.tracer.ResponseDumpFilter");
        responseDumpFilterConfig.setUrlPattern(new String[]{"/*"});
        filterConfigurations.add(responseDumpFilterConfig);
        return filterConfigurations;
    }
}
