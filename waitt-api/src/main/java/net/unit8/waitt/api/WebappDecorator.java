package net.unit8.waitt.api;

import net.unit8.waitt.api.configuration.FilterConfiguration;

import java.util.List;

/**
 * @author kawasima
 */
public interface WebappDecorator {
    List<FilterConfiguration> getFilterConfigs();
}
