package net.unit8.waitt.feature.jacoco;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.logging.Logger;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;

/**
 *
 * @author kawasima
 */
public class JacocoClassLoader extends URLClassLoader {
    private static final Logger logger = Logger.getLogger(JacocoClassLoader.class.getName());
    private static JacocoClassLoader instance;
    private Set<String> targetPackages;

    private Instrumenter instrumenter = null;

    private JacocoClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
        initInstrumenter();
    }

    public static synchronized JacocoClassLoader create(ClassLoader parent) {
        if (instance == null) {
            instance = new JacocoClassLoader(parent);
        }
        return instance;
    }

    public static JacocoClassLoader getInstance() {
        if (instance == null) {
            throw new IllegalStateException("JacocoClassLoader hasn't been instantiated yet.");
        }
        return instance;
    }

    private void initInstrumenter() {
        instrumenter = new Instrumenter(new OfflineInstrumentationAccessGenerator());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class loadClass(final String className, boolean resolve)
            throws ClassNotFoundException {

        Class clazz = findLoadedClass(className);
        if (clazz != null) {
            return clazz;
        }
        Boolean isTargetPackage = false;
        for (String pkgName : targetPackages) {
            isTargetPackage |= className.startsWith(pkgName);
        }
        if (isTargetPackage) {
            logger.fine("[ClassLoad] " + className + " from JaCoCoLoader");
            return defineClass(className, resolve);
        } else {
            return getParent().loadClass(className);
        }
    }

    private Class defineClass(String className, boolean resolve) throws ClassNotFoundException {
        Class clazz = null;
        String path = className.replace('.', '/') + ".class";
        InputStream is = getParent().getResourceAsStream(path);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32768);
        try {
            instrumenter.instrument(is, baos, path);
            clazz = defineClass(className, baos.toByteArray());
            if (resolve) {
                resolveClass(clazz);
            }

        } catch(Throwable t) {
            throw new ClassNotFoundException(className + " from " + getParent(), t);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                //ignore
            }
        }

        return clazz;
    }

    Class defineClass(String className, byte[] bytes) {
        return defineClass(className, bytes, 0, bytes.length);
    }

    public Instrumenter getInstrumenter() {
        return instrumenter;
    }

    public void setTargetPackages(Set<String> targetPackages) {
        this.targetPackages = targetPackages;
    }
}
