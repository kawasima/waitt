package net.unit8.waitt.api;

/**
 *
 * @author kawasima
 */
public interface ClassLoaderFactory {
    ClassLoader create(ClassLoader parent);
}
