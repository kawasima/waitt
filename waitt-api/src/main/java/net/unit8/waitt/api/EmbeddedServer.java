package net.unit8.waitt.api;

import java.util.List;

/**
 * The api of an embedded server.
 *
 * @author kawasima
 */
public interface EmbeddedServer {
    /**
     * Get a name of this embedded server.
     *
     * @return Server name
     */
    String getName();

    /**
     * Set a port number of this embedded server.
     *
     * @param port port number
     */
    void setPort(int port);

    /**
     * Set a root directory of server.
     *
     * @param baseDir base directory
     */
    void setBaseDir(String baseDir);

    /**
     *
     */
    void setWebappDecorators(List<WebappDecorator> decorators);

    /**
     * Set a main context.
     *
     * @param contextPath a path of context
     * @param baseDir a base directory
     * @param loader a classloader for main application
     */
    void setMainContext(String contextPath, String baseDir, ClassLoader loader);
    void addContext(String contextPath, String baseDir, ClassLoader loader);
    void setClassLoaderFactory(ClassLoaderFactory factory);

    /**
     * Start an embedded server.
     */
    void start();

    /**
     * Reload a main context.
     */
    void reload();

    /**
     * Get status.
     */
    ServerStatus getStatus();
    /**
     * Wait for a main thread.
     */
    void await();

    /**
     * Stop an embedded server.
     */
    void stop();

    /**
     * Set a callback to receive request completion events.
     * Called by the server for each HTTP request with method, path, status, and duration.
     *
     * @param listener the request listener, or null to disable
     */
    default void setRequestListener(RequestListener listener) {
        // Default no-op for backward compatibility
    }

    /**
     * Callback interface for HTTP request events.
     */
    interface RequestListener {
        void onRequest(String method, String path, int status, long durationMs);
    }
}
