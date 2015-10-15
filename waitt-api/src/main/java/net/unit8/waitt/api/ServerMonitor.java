package net.unit8.waitt.api;

/**
 *
 * @author kawasima
 */
public interface ServerMonitor {
    void init(EmbeddedServer server);
    void start(EmbeddedServer server);
    void stop();
}
