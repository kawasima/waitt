package net.unit8.waitt.server.tomcat7;

import org.apache.catalina.loader.WebappClassLoader;

/**
 *
 */
public class Tomcat7WebappClassLoaderWrapper extends WebappClassLoader {
    public Tomcat7WebappClassLoaderWrapper(ClassLoader parent) {
        super(ClassLoaderFactoryHolder.getClassLoaderFactory().create(parent));
    }
}
