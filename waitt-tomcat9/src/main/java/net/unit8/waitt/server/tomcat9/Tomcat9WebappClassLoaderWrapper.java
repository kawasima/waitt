package net.unit8.waitt.server.tomcat9;

import org.apache.catalina.loader.WebappClassLoader;

public class Tomcat9WebappClassLoaderWrapper extends WebappClassLoader {
    public Tomcat9WebappClassLoaderWrapper(ClassLoader parent) {
        super(createClassLoader(parent));
    }

    private static ClassLoader createClassLoader(ClassLoader parent) {
        net.unit8.waitt.api.ClassLoaderFactory factory = ClassLoaderFactoryHolder.getClassLoaderFactory();
        if (factory == null) {
            throw new IllegalStateException("ClassLoaderFactory has not been set");
        }
        return factory.create(parent);
    }
}
