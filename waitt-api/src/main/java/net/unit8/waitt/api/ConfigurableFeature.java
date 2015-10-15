package net.unit8.waitt.api;

import net.unit8.waitt.api.configuration.WebappConfiguration;

/**
 *
 * @author kawasima
 */
public interface ConfigurableFeature {
    void config(WebappConfiguration config);
}
