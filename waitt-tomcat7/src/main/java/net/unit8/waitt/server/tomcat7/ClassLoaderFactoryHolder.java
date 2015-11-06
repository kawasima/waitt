package net.unit8.waitt.server.tomcat7;

import net.unit8.waitt.api.ClassLoaderFactory;

/**
 *
 * @author kawasima
 */
public class ClassLoaderFactoryHolder {
    private ClassLoaderFactory factory;
    private static final ClassLoaderFactoryHolder INSTANCE = new ClassLoaderFactoryHolder();
    
    private ClassLoaderFactoryHolder() {
        
    }
    
    public static ClassLoaderFactory getClassLoaderFactory() {
        return INSTANCE.factory;
    }
    
    public static void setClassLoaderFactory(ClassLoaderFactory factory) {
        synchronized(ClassLoaderFactoryHolder.class) {
            INSTANCE.factory = factory;
        }
    }
}
