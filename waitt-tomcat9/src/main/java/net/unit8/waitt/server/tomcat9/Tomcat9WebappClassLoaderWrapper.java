package net.unit8.waitt.server.tomcat9;

import org.apache.catalina.loader.WebappClassLoader;

public class Tomcat9WebappClassLoaderWrapper extends WebappClassLoader {
    public Tomcat9WebappClassLoaderWrapper(ClassLoader parent) {
        super(ClassLoaderFactoryHolder.getClassLoaderFactory().create(parent));
    }
}
