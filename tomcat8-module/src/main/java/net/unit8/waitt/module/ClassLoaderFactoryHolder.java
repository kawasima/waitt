package net.unit8.waitt.module;

import net.unit8.waitt.ClassLoaderFactory;

/**
 *
 * @author kawasima
 */
public class ClassLoaderFactoryHolder {
    private ClassLoaderFactory factory;
    private static ClassLoaderFactoryHolder INSTANCE = new ClassLoaderFactoryHolder();
    
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
