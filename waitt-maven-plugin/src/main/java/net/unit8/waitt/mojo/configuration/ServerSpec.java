package net.unit8.waitt.mojo.configuration;

import net.unit8.waitt.api.EmbeddedServer;

/**
 * @author kawasima
 */
public class ServerSpec {
    private final EmbeddedServer embeddedServer;
    private final ClassLoader classLoader;

    public ServerSpec(EmbeddedServer embeddedServer, ClassLoader classLoader) {
        this.embeddedServer = embeddedServer;
        this.classLoader = classLoader;
    }

    public EmbeddedServer getEmbeddedServer() {
        return embeddedServer;
    }
    public ClassLoader getClassLoader() {
        return classLoader;
    }

}
