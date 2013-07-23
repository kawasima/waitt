package net.unit8.waitt;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * @author kawasima
 */
public class ParentLastClassLoader extends URLClassLoader{
    static Method findLoadedClassMethod;
    static {
        try {
            findLoadedClassMethod = ClassLoader.class
                    .getDeclaredMethod("findLoadedClass", new Class[] { String.class });
            findLoadedClassMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
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
     *              (name.equals("net.unit8.waitt.Instrumenter")
     || name.equals("javax.transaction.xa.XAResource"))) {

     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    public Class<?> loadClass( String name ) throws ClassNotFoundException
    {
        // First check whether it's already been loaded, if so use it
        Class loadedClass = findLoadedClass(name);

        if (loadedClass == null) {
            try {
                loadedClass = (Class<?>)findLoadedClassMethod.invoke(getParent(), name);
            } catch (Exception e) { /* ignore */ }
        }

        // Not loaded, try to load it
        if (name.startsWith("java.") || name.startsWith("javax.")) {
            loadedClass = super.loadClass(name);
        }
        if( loadedClass == null ) {
            try {
                // Ignore parent delegation and just try to load locally
                loadedClass = findClass( name );
            } catch( ClassNotFoundException e ) { /* ignore */ }

            // If not found locally, use normal parent delegation in URLClassloader
            if( loadedClass == null )
            {
                // throws ClassNotFoundException if not found in delegation hierarchy at all
                loadedClass = super.loadClass( name );
            }
        }
        // will never return null (ClassNotFoundException will be thrown)
        return loadedClass;
    }
}
