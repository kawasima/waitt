package net.unit8.waitt.server.jetty12;

import net.unit8.waitt.api.ClassLoaderFactory;

/**
 * @author kawasima
 */
public class ClassLoaderFactoryHolder {
    private volatile ClassLoaderFactory factory;
    private static final ClassLoaderFactoryHolder INSTANCE = new ClassLoaderFactoryHolder();

    private ClassLoaderFactoryHolder() {
    }

    public static ClassLoaderFactory getClassLoaderFactory() {
        return INSTANCE.factory;
    }

    public static void setClassLoaderFactory(ClassLoaderFactory factory) {
        INSTANCE.factory = factory;
    }
}
