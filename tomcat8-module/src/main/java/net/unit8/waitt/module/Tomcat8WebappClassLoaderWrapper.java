package net.unit8.waitt.module;

import net.unit8.waitt.CoberturaClassLoader;
import org.apache.catalina.loader.WebappClassLoader;

/**
 *
 */
public class Tomcat8WebappClassLoaderWrapper extends WebappClassLoader {
    public Tomcat8WebappClassLoaderWrapper(ClassLoader parent) {
        super(CoberturaClassLoader.create(parent));
    }
}
