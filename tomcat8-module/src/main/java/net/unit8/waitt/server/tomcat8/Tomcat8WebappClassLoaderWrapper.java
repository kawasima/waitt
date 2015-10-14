package net.unit8.waitt.server.tomcat8;

import org.apache.catalina.loader.WebappClassLoader;

/**
 *
 */
public class Tomcat8WebappClassLoaderWrapper extends WebappClassLoader {
    public Tomcat8WebappClassLoaderWrapper(ClassLoader parent) {
        super(ClassLoaderFactoryHolder.getClassLoaderFactory().create(parent));
    }
}
