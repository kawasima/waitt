package net.unit8.waitt.server.tomcat85;

import org.apache.catalina.loader.WebappClassLoader;

/**
 *
 */
public class Tomcat85WebappClassLoaderWrapper extends WebappClassLoader {
    public Tomcat85WebappClassLoaderWrapper(ClassLoader parent) {
        super(ClassLoaderFactoryHolder.getClassLoaderFactory().create(parent));
    }
}
