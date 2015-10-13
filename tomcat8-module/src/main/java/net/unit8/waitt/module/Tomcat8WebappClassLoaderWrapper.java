package net.unit8.waitt.module;

import org.apache.catalina.loader.WebappClassLoader;

/**
 *
 */
public class Tomcat8WebappClassLoaderWrapper extends WebappClassLoader {
    public Tomcat8WebappClassLoaderWrapper(ClassLoader parent) {
        super(ClassLoaderFactoryHolder.getClassLoaderFactory().create(parent));
    }
}
