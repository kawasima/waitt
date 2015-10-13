package net.unit8.waitt.feature;

import net.unit8.waitt.EmbeddedServer;

/**
 *
 * @author kawasima
 */
public interface ServerMonitor {
    void config(EmbeddedServer server);
    void start(EmbeddedServer server);
    void stop();
}
