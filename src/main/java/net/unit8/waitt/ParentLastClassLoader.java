package net.unit8.waitt;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.logging.Logger;

/**
 * @author kawasima
 */
public class ParentLastClassLoader extends URLClassLoader{
    private static final Logger logger = Logger.getLogger(ParentLastClassLoader.class.getName());
    static final Method findLoadedClassMethod;
    static final Method findBootstrapClassOrNullMethod;

    static {
        try {
            findLoadedClassMethod = ClassLoader.class
                    .getDeclaredMethod("findLoadedClass", new Class[] { String.class });
            findLoadedClassMethod.setAccessible(true);
            findBootstrapClassOrNullMethod = ClassLoader.class
                    .getDeclaredMethod("findBootstrapClassOrNull", new Class[] { String.class});
            findBootstrapClassOrNullMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public ParentLastClassLoader( URL[] urls, ClassLoader parent )
    {
        super( urls, parent );
    }

    public ParentLastClassLoader( URL[] urls )
    {
        super( urls );
    }

    public ParentLastClassLoader( URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory )
    {
        super( urls, parent, factory );
    }

    /**
     * Load classes.
     *
     * @param name the name of class.
     * @return loaded class
     * @throws ClassNotFoundException
     */
    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        // First check whether it's already been loaded, if so use it
        synchronized (this) {
            Class loadedClass = findLoadedClass(name);

            if (loadedClass == null) {
                try {
                    ClassLoader parent = getParent();
                    loadedClass = (Class<?>) findBootstrapClassOrNullMethod.invoke(getParent(), name);

                    while(parent != null && loadedClass == null) {
                        loadedClass = (Class<?>) findLoadedClassMethod.invoke(parent, name);
                        parent = parent.getParent();
                    }
                } catch (Exception e) { /* ignore */ }
            }

            // Not loaded, try to load it
            if( loadedClass == null ) {
                try {
                    // Ignore parent delegation and just try to load locally
                    if (name.equals("net.unit8.waitt.Instrumenter"))
                        throw new ClassNotFoundException("");
                    loadedClass = findClass(name);
                    logger.fine("[loaded] " + name + " from ParentLastClassLoader");
                    if (resolve)
                        resolveClass(loadedClass);
                } catch( ClassNotFoundException e) {
                    /* ignore */
                } catch(IllegalAccessError e) {
                    /* Load from parent loader */
                    loadedClass = getParent().loadClass(name);
                    logger.fine("[loaded] " + name +" from WebappClassLoader");
                }

                // If not found locally, use normal parent delegation in URLClassloader
                if( loadedClass == null ) {
                    // throws ClassNotFoundException if not found in delegation hierarchy at all
                    loadedClass = super.loadClass(name, resolve);
                    logger.fine("[loaded] " + name + " from WebappClassLoader");
                }
            }
            // will never return null (ClassNotFoundException will be thrown)
            return loadedClass;
        }
    }
}
