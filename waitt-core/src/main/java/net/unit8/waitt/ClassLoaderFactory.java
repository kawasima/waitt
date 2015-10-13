package net.unit8.waitt;

/**
 *
 * @author kawasima
 */
public interface ClassLoaderFactory {
    ClassLoader create(ClassLoader parent);
}
