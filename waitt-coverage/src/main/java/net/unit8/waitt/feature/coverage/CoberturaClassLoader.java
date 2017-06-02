package net.unit8.waitt.feature.coverage;

import net.sourceforge.cobertura.util.IOUtil;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author kawasima
 */
@SuppressWarnings("rawtypes")
public class CoberturaClassLoader extends ClassLoader {
    private static final Logger logger = Logger.getLogger(CoberturaClassLoader.class.getName());
    private static CoberturaClassLoader instance;
    private Set<String> targetPackages;

    private final Collection<Pattern> ignoreRegexes = new Vector<Pattern>();
    private final Set<String> ignoreMethodAnnotations = new HashSet<String>();

    private Instrumenter instrumenter = null;

    private CoberturaClassLoader(ClassLoader parent) {
        super(parent);
        initInstrumenter();
    }

    public static synchronized CoberturaClassLoader create(ClassLoader parent) {
        if (instance == null) {
            instance = new CoberturaClassLoader(parent);
        }
        return instance;
    }

    public static CoberturaClassLoader getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CoberturaClassLoader hasn't been instantiated yet.");
        }
        return instance;
    }

    private void initInstrumenter() {
        instrumenter = new CoberturaInstrumenterWrapper();
        instrumenter.setIgnoreRegexes(ignoreRegexes);
        instrumenter.setIgnoreTrivial(false);
        instrumenter
                .setIgnoreMethodAnnotations(ignoreMethodAnnotations);
        instrumenter.setThreadsafeRigorous(false);
        instrumenter.setFailOnError(false);
    }

    @SuppressWarnings("unchecked")
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
            logger.fine("[loaded] " + className + " from CoberturaClassLoader");
            return defineClass(className, resolve);
        } else {
            return getParent().loadClass(className);
        }
    }

    private Class defineClass(String className, boolean resolve) throws ClassNotFoundException {
        Class clazz = null;
        String path = className.replace('.', '/') + ".class";
        InputStream is = getParent().getResourceAsStream(path);

        byte[] instrumentationResult;
        try {
            instrumentationResult = instrumenter.instrumentClassByte(is);
            if (instrumentationResult == null) {
                return getParent().loadClass(className);
            }
        } catch(Throwable t) {
            throw new ClassNotFoundException(className + " from " + getParent(), t);
        } finally {
            IOUtil.closeInputStream(is);
        }

        if (is != null) {
            clazz = defineClass(className, instrumentationResult);
            if (resolve) {
                resolveClass(clazz);
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
