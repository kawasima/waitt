package net.unit8.waitt.server.tomcat10;

import org.apache.catalina.loader.WebappClassLoader;

public class Tomcat10WebappClassLoaderWrapper extends WebappClassLoader {
    public Tomcat10WebappClassLoaderWrapper(ClassLoader parent) {
        super(ClassLoaderFactoryHolder.getClassLoaderFactory().create(parent));
    }
}
