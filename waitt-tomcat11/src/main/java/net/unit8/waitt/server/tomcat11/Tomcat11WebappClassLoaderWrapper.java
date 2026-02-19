package net.unit8.waitt.server.tomcat11;

import org.apache.catalina.loader.WebappClassLoader;

public class Tomcat11WebappClassLoaderWrapper extends WebappClassLoader {
    public Tomcat11WebappClassLoaderWrapper(ClassLoader parent) {
        super(ClassLoaderFactoryHolder.getClassLoaderFactory().create(parent));
    }
}
